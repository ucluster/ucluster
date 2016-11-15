package com.github.ucluster.mongo;

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
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.HashMap;
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
    AuthenticationServiceRegistry registry;

    @Override
    public User create(Map<String, Object> request) {
        final CreateUserRequest createUserRequest = new CreateUserRequest(request);

        final MongoUser user = new MongoUser();
        user.metadata = createUserRequest.metadata();

        enhance(user);
        createUserRequest.properties().keySet().stream()
                .forEach(propertyPath -> user.property(
                        propertyPath,
                        createUserRequest.properties().get(propertyPath))
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
    public Optional<User> authenticate(Map<String, Object> request) {
        Optional<AuthenticationService> service = registry.find(methodOf(request));

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(request);

        audit(request, response);

        if (response.status() == FAILED) {
            return Optional.empty();
        }

        return response.candidate();
    }

    private static class CreateUserRequest {

        private final Map<String, Object> request;

        CreateUserRequest(Map<String, Object> request) {
            this.request = request;
        }

        Map<String, Object> metadata() {
            Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", new HashMap<>());
            metadata = new HashMap<>(metadata);

            metadata.put("model", Constants.Record.USER);
            if (!metadata.containsKey("type")) {
                metadata.put("type", "default");
            }
            if (!metadata.containsKey("user_type")) {
                metadata.put("user_type", metadata.get("type"));
            }

            return metadata;
        }

        Map<String, Object> properties() {
            return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
        }
    }

    private void audit(Map<String, Object> request, AuthenticationResponse response) {
        User.AuthenticationLog authenticationLog = new MongoAuthenticationLog(request, response);
        injector.injectMembers(authenticationLog);
        authenticationLog.save();
    }

    private String methodOf(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return (String) metadata.getOrDefault("method", "password");
    }
}
