package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Optional;

public class MongoUserRepository implements UserRepository {
    @Inject
    protected Datastore datastore;

    @Inject
    protected UserDefinitionRepository userDefinitions;

    @Override
    public User create(User.Request request) {
        final UserDefinition userDefinition = userDefinitions.find(request.metadata());
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
                .field(MongoUserProperty.valueMongoField(property)).equal(property.value())
                .get();

        return enhance(user);
    }

    @Override
    public User update(User user) {
        ((MongoUser) user).flush();
        return user;
    }

    private Optional<User> enhance(MongoUser user) {
        if (user == null) {
            return Optional.empty();
        }

        user.definition = userDefinitions.find(user.metadata);
        user.datastore = datastore;
        return Optional.of(user);
    }

    private User constructUser(UserDefinition userDefinition, User.Request request) {
        final MongoUser user = new MongoUser.Builder(userDefinition).create(request);

        return enhance(user).get();
    }

    private void ensureRequestMatchUserDefinition(UserDefinition userDefinition, User.Request request) {
        final ValidationResult validationResult = userDefinition.validate(request.properties());

        if (!validationResult.valid()) {
            throw new UserValidationException(validationResult);
        }
    }
}
