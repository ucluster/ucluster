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

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MongoUserRepository implements UserRepository {
    @Inject
    protected Datastore datastore;

    @Inject
    protected UserDefinitionRepository userDefinitions;

    @Override
    public User create(Map<String, Object> request) {
        final UserDefinition userDefinition = userDefinitions.find(request);
        ensureRequestMatchUserDefinition(userDefinition, request);

        final User user = constructUser(userDefinition, request);
        datastore.save(user);
        return user;
    }

    private MongoUser constructUser(UserDefinition userDefinition, Map<String, Object> request) {
        final MongoUser user = new MongoUser();
        user.createdAt = new DateTime();

        getProperties(request).keySet().forEach(propertyKey -> {
            final UserDefinition.PropertyDefinition propertyDefinition = userDefinition.property(propertyKey);

            user.update(constructProperty(propertyDefinition, (String) getProperties(request).get(propertyKey)));
        });
        return user;
    }

    private User.Property constructProperty(UserDefinition.PropertyDefinition propertyDefinition, String propertyValue) {
        if (Objects.equals(propertyDefinition.definition().get("password"), true)) {
            return new MongoUserProperty(propertyDefinition.propertyPath(), Encryption.BCRYPT.encrypt(propertyValue));
        }
        return new MongoUserProperty(propertyDefinition.propertyPath(), propertyValue);
    }

    private void ensureRequestMatchUserDefinition(UserDefinition userDefinition, Map<String, Object> request) {
        final ValidationResult validationResult = userDefinition.validate(getProperties(request));

        if (!validationResult.valid()) {
            throw new UserValidationException(validationResult);
        }
    }

    private Map<String, Object> getProperties(Map<String, Object> request) {
        return (Map<String, Object>) request.get("properties");
    }

    @Override
    public User uuid(String uuid) {
        return datastore.get(MongoUser.class, new ObjectId(uuid));
    }

    @Override
    public Optional<User> find(User.Property property) {
        //disable validation since the mismatch for java object and mongo storage
        final User user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field("properties." + property.key() + ".value").equal(property.value())
                .get();

        return Optional.ofNullable(user);
    }
}
