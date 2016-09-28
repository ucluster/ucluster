package com.github.ucluster.mongo;

import com.github.ucluster.common.definition.processor.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.exception.UserAuthenticationException;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.UpdateOperations;

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

    @Transient
    protected UserDefinition definition;

    @Transient
    protected Datastore datastore;

    @Transient
    protected DirtyTracker dirtyTracker = new DirtyTracker();

    MongoUser() {
    }

    MongoUser(DateTime createdAt, Map<String, Object> metadata, List<Property> properties, UserDefinition definition) {
        this.createdAt = createdAt;
        this.metadata = metadata;
        this.definition = definition;
        this.properties = properties.stream()
                .collect(
                        Collectors.toMap(
                                Property::key,
                                property -> process(property, PropertyProcessor.Type.BEFORE_CREATE)
                        ));
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
        final Property propertyToUpdate = process(property, PropertyProcessor.Type.BEFORE_UPDATE);
        dirtyTracker.dirty(propertyToUpdate);
        properties.put(property.key(), propertyToUpdate);
    }

    @Override
    public Optional<Property> property(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    protected Property process(Property property, PropertyProcessor.Type beforeCreate) {
        return definition.property(property.key()).process(beforeCreate, property);
    }

    protected void flush() {
        dirtyTracker.flush();
    }

    @Transient
    protected Map<String, Property> dirtyProperties = new HashMap<>();

    private class DirtyTracker {
        Map<String, Property> dirtyProperties = new HashMap<>();

        void dirty(Property property) {
            dirtyProperties.put(property.key(), property);
        }

        void flush() {
            datastore.update(MongoUser.this, generateDirtyUpdateOperations());
        }

        private UpdateOperations<User> generateDirtyUpdateOperations() {
            final UpdateOperations<User> operations = datastore.createUpdateOperations(User.class)
                    .disableValidation();

            dirtyProperties.entrySet().stream().forEach(e ->
                    operations.set(MongoUserProperty.mongoField(e.getValue()), e.getValue())
            );

            dirtyProperties.clear();

            return operations;
        }
    }

    static class Builder {
        private UserDefinition definition;

        Builder(UserDefinition definition) {
            this.definition = definition;
        }

        MongoUser create(Request request) {
            final List<User.Property> properties = request.properties().keySet().stream()
                    .map(propertyKey -> new MongoUserProperty<>(definition.property(propertyKey).propertyPath(), request.properties().get(propertyKey)))
                    .collect(Collectors.toList());

            return new MongoUser(new DateTime(), request.metadata(), properties, definition);
        }
    }
}
