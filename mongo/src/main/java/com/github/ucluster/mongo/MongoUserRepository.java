package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;
import com.github.ucluster.mongo.security.Encryption;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoUserRepository implements UserRepository {
    @Inject
    protected Datastore datastore;

    @Inject
    protected UserDefinitionRepository userDefinitions;

    @Override
    public User create(Map<String, Object> request) {
        final UserDefinition userDefinition = userDefinitions.find(getMetadata(request));
        ensureRequestMatchUserDefinition(userDefinition, request);

        final User user = constructUser(userDefinition, request);
        datastore.save(user);
        return user;
    }

    @Override
    public User uuid(String uuid) {
        return datastore.get(MongoUser.class, new ObjectId(uuid));
    }

    @Override
    public Optional<User> find(User.Property property) {
        final User user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field("properties." + property.key() + ".value").equal(property.value())
                .get();

        return Optional.ofNullable(user);
    }

    @Override
    public User update(User user) {
        datastore.update(user, getDirtyUpdateOperations(user));
        return user;
    }

    private MongoUser constructUser(UserDefinition userDefinition, Map<String, Object> request) {
        final List<User.Property> properties = getProperties(request).keySet().stream()
                .map(propertyKey -> constructProperty(userDefinition.property(propertyKey), (String) getProperties(request).get(propertyKey)))
                .collect(Collectors.toList());

        return new MongoUser(new DateTime(), getMetadata(request), properties);
    }

    private User.Property constructProperty(UserDefinition.PropertyDefinition propertyDefinition, String propertyValue) {
        if (Objects.equals(propertyDefinition.definition().get("password"), true)) {
            return new MongoUserProperty<>(propertyDefinition.propertyPath(), Encryption.BCRYPT.encrypt(propertyValue));
        }
        return new MongoUserProperty<>(propertyDefinition.propertyPath(), propertyValue);
    }

    private void ensureRequestMatchUserDefinition(UserDefinition userDefinition, Map<String, Object> request) {
        final ValidationResult validationResult = userDefinition.validate(getProperties(request));

        if (!validationResult.valid()) {
            throw new UserValidationException(validationResult);
        }
    }

    private Map<String, Object> getProperties(Map<String, Object> request) {
        return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
    }

    private Map<String, Object> getMetadata(Map<String, Object> request) {
        final Map<Object, Object> defaultMetadata = new HashMap<>();
        defaultMetadata.put("type", "default");

        return (Map<String, Object>) request.getOrDefault("metadata", defaultMetadata);
    }

    private UpdateOperations<User> getDirtyUpdateOperations(User user) {
        MongoUser updateUser = (MongoUser) user;

        ensureNoImmutablePropertyToUpdate(updateUser);

        final UpdateOperations<User> operations = datastore.createUpdateOperations(User.class)
                .disableValidation();

        updateUser.dirtyProperties.entrySet().stream().forEach(e ->
                operations.set("properties." + e.getKey(), e.getValue())
        );

        updateUser.dirtyProperties.clear();

        return operations;
    }

    private void ensureNoImmutablePropertyToUpdate(MongoUser user) {
        final UserDefinition userDefinition = userDefinitions.find(user.metadata);

        user.dirtyProperties.values().forEach(property -> {
            final UserDefinition.PropertyDefinition propertyDefinition = userDefinition.property(property.key());

            if (propertyDefinition.definition().getOrDefault("immutable", false).equals(true)) {
                throw new UserValidationException(new ValidationResult(new ValidationResult.ValidateFailure(property.key(), "immutable")));
            }
        });
    }
}
