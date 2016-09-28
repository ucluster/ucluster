package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Optional<User> uuid(String uuid) {
        return enhance(datastore.get(MongoUser.class, new ObjectId(uuid)));
    }

    @Override
    public Optional<User> find(User.Property property) {
        final MongoUser user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field("properties." + property.key() + ".value").equal(property.value())
                .get();

        return enhance(user);
    }

    private Optional<User> enhance(MongoUser user) {
        if (user == null) {
            return Optional.empty();
        }

        user.definition = userDefinitions.find(user.metadata);
        user.datastore = datastore;
        return Optional.of(user);
    }

    @Override
    public User update(User user) {
        ((MongoUser) user).flush();
        return user;
    }

    private MongoUser constructUser(UserDefinition userDefinition, Map<String, Object> request) {
        final List<User.Property> properties = getProperties(request).keySet().stream()
                .map(propertyKey -> constructProperty(userDefinition.property(propertyKey), (String) getProperties(request).get(propertyKey)))
                .collect(Collectors.toList());

        final MongoUser user = new MongoUser(new DateTime(), getMetadata(request), properties, userDefinition);
        user.definition = userDefinition;
        user.datastore = datastore;
        return user;
    }

    private User.Property constructProperty(UserDefinition.PropertyDefinition propertyDefinition, String propertyValue) {
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
}
