package com.github.ucluster.mongo;

import com.github.ucluster.common.definition.processor.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    MongoUser(Map<String, Object> metadata, List<Property> properties, UserDefinition definition) {
        this.createdAt = new DateTime();
        this.metadata = metadata;
        this.definition = definition;
        this.properties = properties.stream()
                .collect(
                        Collectors.toMap(
                                Property::path,
                                property -> property)
                );
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
        final Optional<Property> property = property(identityProperty.path());

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
        dirtyTracker.dirty(property.path());
        properties.put(property.path(), property);
    }

    @Override
    public Optional<Property> property(String propertyPath) {
        return Optional.ofNullable(properties.get(propertyPath));
    }

    protected void flush() {
        dirtyTracker.flush();
    }

    //TODO: hide the BEFORE_CREATE / BEFORE_UPDATE logic in MongUser
    protected ValidationResult validate(PropertyProcessor.Type processType) {
        if (processType == PropertyProcessor.Type.BEFORE_CREATE) {
            return definition.validate(this);
        } else if (processType == PropertyProcessor.Type.BEFORE_UPDATE) {
            return properties.keySet().stream()
                    .filter(propertyPath -> dirtyTracker.isDirty(propertyPath))
                    .map(propertyPath -> definition.property(propertyPath))
                    .map(propertyDefinition -> propertyDefinition.validate(this))
                    .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
        }

        throw new RuntimeException("not supported process type");
    }

    //TODO: hide the BEFORE_CREATE / BEFORE_UPDATE logic in MongUser
    protected void process(PropertyProcessor.Type processType) {
        if (processType == PropertyProcessor.Type.BEFORE_CREATE) {
            properties.keySet().forEach(propertyPath -> {
                Property property = properties.get(propertyPath);
                properties.put(propertyPath, definition.property(property.path()).process(processType, property));
            });
        } else if (processType == PropertyProcessor.Type.BEFORE_UPDATE) {
            properties.keySet().stream()
                    .filter(propertyPath -> dirtyTracker.isDirty(propertyPath))
                    .forEach(propertyPath -> {
                        Property property = properties.get(propertyPath);
                        properties.put(propertyPath, definition.property(property.path()).process(processType, property));
                    });
        }
    }

    private class DirtyTracker {
        Set<String> dirtyProperties = new HashSet<>();

        void dirty(String propertyPath) {
            dirtyProperties.add(propertyPath);
        }

        void flush() {
            datastore.update(MongoUser.this, generateDirtyUpdateOperations());
        }

        private UpdateOperations<User> generateDirtyUpdateOperations() {
            final UpdateOperations<User> operations = datastore.createUpdateOperations(User.class)
                    .disableValidation();

            dirtyProperties.stream()
                    .map(MongoUser.this::property)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(property ->
                            operations.set(MongoUserProperty.mongoField(property), property)
                    );

            dirtyProperties.clear();

            return operations;
        }

        boolean isDirty(String propertyPath) {
            return dirtyProperties.contains(propertyPath);
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

            return new MongoUser(request.metadata(), properties, definition);
        }
    }
}
