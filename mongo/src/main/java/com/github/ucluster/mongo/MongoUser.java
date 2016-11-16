package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.github.ucluster.session.Session;
import com.google.common.collect.ImmutableMap;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.ucluster.mongo.Constants.Collection.USERS;

@Entity(USERS)
public class MongoUser extends MongoRecord<User> implements User, Model {

    @Inject
    @Transient
    RequestFactory requestFactory;

    @Inject
    @Transient
    Session session;

    @Override
    public User.Request apply(ApiRequest request) {
        final MongoRequest req = saveRequest(request);
        req.execute(request);
        return req;
    }

    @Override
    public Optional<User.Request> request(String uuid) {
        try {
            final MongoRequest request = datastore.get(MongoRequest.class, new ObjectId(uuid));

            if (request == null || !request.user.uuid().equals(uuid())) {
                return Optional.empty();
            }

            enhance(request);
            return Optional.of(request);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public PaginatedList<Request> requests(Criteria criteria) {
        final Query<MongoRequest> query = datastore.createQuery(MongoRequest.class)
                .disableValidation()
                .field("user").equal(new Key<>(MongoUser.class, USERS, uuid));

        criteria.params(e -> {
            query.field(MongoProperty.valueMongoField(e.getKey())).in(e.getValue());
        });

        return new PaginatedList<>(query.countAll(),
                (page, perPage) -> query
                        .order("-createdAt")
                        .offset((page - 1) * perPage)
                        .limit(perPage)
                        .asList()
                        .stream()
                        .map(this::enhance)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Token generateToken() {
        clearExistToken();
        return createNewToken();
    }

    @Override
    public Optional<Token> currentToken() {
        final Optional<Object> o = session.get(Keys.user_token(this));

        if (!o.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new UserToken((Map<String, Object>) o.get()));
    }

    private Token createNewToken() {
        return store(new UserToken(generateRandomToken(), generateRandomToken()));
    }

    private Token store(UserToken token) {
        session.setex(token.accessToken, super.toJson(), Constants.Token.ACCESS_EXPIRE_SECONDS);
        session.setex(token.refreshToken, ImmutableMap.<String, Object>builder()
                .put("access_token", token.accessToken)
                .put("id", uuid())
                .build(), Constants.Token.REFRESH_EXPIRE_SECONDS);
        session.setex(Keys.user_token(this), ImmutableMap.<String, Object>builder()
                .put("access_token", token.accessToken)
                .put("refresh_token", token.refreshToken)
                .build(), Constants.Token.REFRESH_EXPIRE_SECONDS);

        return token;
    }

    private void clearExistToken() {
        session.get(Keys.user_token(this)).ifPresent(currentToken -> {
            Map<String, Object> current = (Map<String, Object>) currentToken;
            session.del((String) current.get("access_token"));
            session.del((String) current.get("refresh_token"));
            session.del(Keys.user_token(this));
        });
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString();
    }

    private MongoRequest enhance(MongoRequest request) {
        request.user = this;
        injector.injectMembers(request);
        return request;
    }

    private MongoRequest saveRequest(ApiRequest request) {
        final MongoRequest req = (MongoRequest) requestFactory.create(this, request);
        enhance(req);
        req.save();
        return req;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = super.toJson();
        json.put("uri", Routing.user(this));

        return json;
    }

    private class UserToken implements User.Token, Model {
        private final String accessToken;
        private final String refreshToken;

        UserToken(Map<String, Object> tokens) {
            this.accessToken = (String) tokens.get("access_token");
            this.refreshToken = (String) tokens.get("refresh_token");
        }

        UserToken(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        @Override
        public String accessToken() {
            return accessToken;
        }

        @Override
        public String refreshToken() {
            return refreshToken;
        }

        @Override
        public Map<String, Object> toJson() {
            return ImmutableMap.<String, Object>builder()
                    .put("access_token", accessToken())
                    .put("refresh_token", refreshToken())
                    .build();
        }

        @Override
        public Map<String, Object> toReferenceJson() {
            return toJson();
        }
    }
}
