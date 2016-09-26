package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.UserValidationException;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.github.ucluster.mongo.security.Encryption;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity("users")
@Converters(JodaDateTimeConverter.class)
public class MongoUser implements User {

    @Id
    protected ObjectId uuid;

    @org.mongodb.morphia.annotations.Property
    protected DateTime createdAt;

    @Embedded
    protected List<Property> properties = new ArrayList<>();

    @Override
    public String uuid() {
        return uuid.toHexString();
    }

    @Override
    public DateTime createdAt() {
        return createdAt;
    }

    @Override
    public void authenticate(Property identityProperty, String password) {
        //check property is identity property or not
        final Optional<Property> property = property(identityProperty.key());

        if (!property.isPresent()) {
            throw new UserValidationException();
        }

        if (!property.get().value().equals(identityProperty.value())) {
            throw new UserValidationException();
        }

        if (!Encryption.BCRYPT.check(password, property("password").get().value())) {
            throw new UserValidationException();
        }
    }

    @Override
    public void update(Property property) {
        properties.add(property);
    }

    @Override
    public Optional<Property> property(String key) {
        return properties.stream().filter(property -> property.key().equals(key)).findFirst();
    }
}
