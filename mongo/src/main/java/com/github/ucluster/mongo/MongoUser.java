package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
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
    public User.Request apply(Map<String, Object> request) {
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
    public Map<String, Object> generateToken() {
        clearExistToken();

        String accessToken = doGenerateToken();
        String refreshToken = doGenerateToken();
        generateNewToken(accessToken, refreshToken);

        return ImmutableMap.<String, Object>builder()
                .put("access_token", accessToken)
                .put("refresh_token", refreshToken)
                .build();
    }

    private void generateNewToken(String accessToken, String refreshToken) {
        session.setex(accessToken, super.toJson(), 30 * 60);
        session.setex(refreshToken, ImmutableMap.<String, Object>builder()
                .put("access_token", accessToken)
                .put("uuid", uuid())
                .build(), 7 * 24 * 60 * 60);
        session.setex(currentUserTokenKey(), ImmutableMap.<String, Object>builder()
                .put("access_token", accessToken)
                .put("refresh_token", refreshToken)
                .build(), 7 * 24 * 60 * 60);
    }

    private void clearExistToken() {
        session.get(currentUserTokenKey()).ifPresent(currentToken -> {
            Map<String, Object> current = (Map<String, Object>) currentToken;
            session.del((String) current.get("access_token"));
            session.del((String) current.get("refresh_token"));
            session.del(currentUserTokenKey());
        });
    }

    private String currentUserTokenKey() {
        return uuid();
    }

    private String doGenerateToken() {
        return UUID.randomUUID().toString();
    }

    private MongoRequest enhance(MongoRequest request) {
        request.user = this;
        injector.injectMembers(request);
        return request;
    }

    private MongoRequest saveRequest(Map<String, Object> request) {
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
}
