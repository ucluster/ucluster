package com.github.ucluster.mongo;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.github.ucluster.mongo.authentication.MongoAuthenticationLog;
import com.github.ucluster.session.Session;
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;

public class MongoUserRepository implements UserRepository {
    @Inject
    protected Datastore datastore;

    @Inject
    protected Injector injector;

    @Inject
    protected Session session;

    @Inject
    AuthenticationServiceRegistry registry;

    @Override
    public User create(ApiRequest request) {
        final MongoUser user = new MongoUser();
        user.metadata = request.metadata();

        enhance(user);
        request.properties().keySet().stream()
                .forEach(propertyPath -> user.property(
                        propertyPath,
                        request.properties().get(propertyPath))
                );

        user.save();
        return user;
    }

    @Override
    public Optional<User> uuid(String uuid) {
        try {
            return enhance(datastore.get(MongoUser.class, new ObjectId(uuid)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findBy(Record.Property property) {
        final MongoUser user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field(MongoProperty.valueMongoField(property)).equal(property.value())
                .get();

        return enhance(user);
    }

    @Override
    public <V> Optional<User> findBy(String propertyPath, V value) {
        return findBy(new MongoProperty<>(propertyPath, value));
    }

    @Override
    public PaginatedList<User> find(Criteria criteria) {
        final Query<MongoUser> query = datastore.createQuery(MongoUser.class)
                .disableValidation();

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
                        .map(user -> enhance(user).get())
                        .collect(Collectors.toList()));
    }

    private Optional<User> enhance(MongoUser user) {
        if (user == null) {
            return Optional.empty();
        }

        injector.injectMembers(user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> authenticate(ApiRequest request) {
        request.model(Constants.Record.USER);
        Optional<AuthenticationService> service = registry.find(request.metadata());

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(request);

        auditAuthenticationLog(request, response);

        if (response.status() == FAILED) {
            throw new AuthenticationException();
        }

        return response.candidate();
    }

    @Override
    public Optional<User> findByAccessToken(String accessToken) {
        final Optional<Object> o = session.get(accessToken);

        if (!o.isPresent()) {
            return Optional.empty();
        }

        return uuid((String) ((Map<String, Object>) o.get()).get("id"));
    }

    private void auditAuthenticationLog(ApiRequest request, AuthenticationResponse response) {
        User.AuthenticationLog authenticationLog = new MongoAuthenticationLog(request, response);
        injector.injectMembers(authenticationLog);
        authenticationLog.save();
    }
}
