package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.mongo.security.Encryption;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class MongoUserRepository implements UserRepository {
    @Inject
    protected Datastore datastore;

    @Override
    public User create(Map<String, Object> request) {
        //validation
        final MongoUser user = new MongoUser();
        user.createdAt = new DateTime();

        final Map<String, Object> properties = (Map<String, Object>) request.get("properties");
        properties.keySet().forEach(key -> {
            user.update(new MongoUserProperty(key, convertPropertyValue(properties, key)));
        });

        datastore.save(user);
        return user;
    }

    private String convertPropertyValue(Map<String, Object> properties, String key) {
        //convert property value, such as password and deal-password, etc..
        final String original = (String) properties.get(key);
        if (key.equals("password")) {
            return Encryption.BCRYPT.encrypt(original);
        }
        return original;
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
