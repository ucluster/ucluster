package com.github.ucluster.mongo;

import com.github.ucluster.core.LifecycleMonitor;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Optional;

public class MongoUserRepository implements Repository<User> {
    @Inject
    protected Datastore datastore;

    @Inject
    protected DefinitionRepository<Definition<User>> definitions;

    @Inject
    protected LifecycleMonitor<User> lifecycleMonitor;

    @Override
    public User create(Record.Request request) {
        final MongoUser user = new MongoUser();
        user.createdAt = new DateTime();
        user.metadata = request.metadata();
        user.definition = definitions.find(request.metadata());

        final User monitored = lifecycleMonitor.monitor(user);
        request.properties().keySet().stream()
                .forEach(propertyPath -> monitored.update(new MongoProperty<>(
                        propertyPath,
                        request.properties().get(propertyPath))
                ));

        monitored.save();
        return monitored;
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

        user.definition = definitions.find(user.metadata());
        return Optional.of(lifecycleMonitor.monitor(user));
    }
}
