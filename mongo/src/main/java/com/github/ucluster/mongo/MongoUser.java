package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.UserAuthenticationException;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.github.ucluster.mongo.security.Encryption;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity("users")
@Converters(JodaDateTimeConverter.class)
public class MongoUser implements User {

    @Id
    protected ObjectId uuid;

    @org.mongodb.morphia.annotations.Property
    protected DateTime createdAt;

    @org.mongodb.morphia.annotations.Property
    protected Map<String, Object> metadata;

    @Embedded
    protected Map<String, Property> properties = new HashMap<>();

    MongoUser() {
    }

    MongoUser(DateTime createdAt, Map<String, Object> metadata, List<Property> properties) {
        this.createdAt = createdAt;
        this.metadata = metadata;
        this.properties = properties.stream().collect(Collectors.toMap(Property::key, property -> property));
    }

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
        final Optional<Property> property = property(identityProperty.key());

        if (!property.isPresent()) {
            throw new UserAuthenticationException();
        }

        if (!property.get().value().equals(identityProperty.value())) {
            throw new UserAuthenticationException();
        }

        if (!Encryption.BCRYPT.check(password, (String) property("password").get().value())) {
            throw new UserAuthenticationException();
        }
    }

    @Override
    public void update(Property property) {
        dirty(property);
        properties.put(property.key(), property);
    }

    @Override
    public Optional<Property> property(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    @Transient
    protected Map<String, Property> dirtyProperties = new HashMap<>();

    protected void dirty(Property property) {
        dirtyProperties.put(property.key(), property);
    }
}
