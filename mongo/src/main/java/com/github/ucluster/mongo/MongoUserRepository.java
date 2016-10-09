package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MongoUserRepository implements Repository<User> {
    @Inject
    protected Datastore datastore;

    @Inject
    protected DefinitionRepository<Definition<User>> definitions;

    @Inject
    protected Injector injector;

    @Override
    public User create(Map<String, Object> request) {
        final CreateUserRequest createUserRequest = new CreateUserRequest(request);

        final MongoUser user = new MongoUser();
        user.createdAt = new DateTime();
        user.metadata = createUserRequest.metadata();

        enhance(user);
        createUserRequest.properties().keySet().stream()
                .forEach(propertyPath -> user.property(new MongoProperty<>(
                        propertyPath,
                        createUserRequest.properties().get(propertyPath))
                ));

        user.save();
        return user;
    }

    @Override
    public Optional<User> uuid(String uuid) {
        return enhance(datastore.get(MongoUser.class, new ObjectId(uuid)));
    }

    @Override
    public Optional<User> find(Record.Property property) {
        final MongoUser user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field(MongoProperty.valueMongoField(property)).equal(property.value())
                .get();

        return enhance(user);
    }

    private Optional<User> enhance(MongoUser user) {
        if (user == null) {
            return Optional.empty();
        }

        injector.injectMembers(user);
        return Optional.of(user);
    }

    private static class CreateUserRequest {

        private final Map<String, Object> request;

        CreateUserRequest(Map<String, Object> request) {
            this.request = request;
        }

        Map<String, Object> metadata() {
            final Map<String, Object> defaultMetadata = new HashMap<>();
            defaultMetadata.put("user_type", "default");

            return (Map<String, Object>) request.getOrDefault("metadata", defaultMetadata);
        }

        Map<String, Object> properties() {
            return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
        }
    }
}
