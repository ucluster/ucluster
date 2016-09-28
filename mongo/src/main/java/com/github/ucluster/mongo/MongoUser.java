package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserAuthenticationException;
import com.github.ucluster.core.exception.UserValidationException;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.github.ucluster.mongo.security.Encryption;
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
import java.util.Objects;
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

    MongoUser() {
    }

    MongoUser(DateTime createdAt, Map<String, Object> metadata, List<Property> properties, UserDefinition definition) {
        this.createdAt = createdAt;
        this.metadata = metadata;
        this.definition = definition;
        this.properties = properties.stream().collect(Collectors.toMap(Property::key, this::encryptPasswordProperty));
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
        ensurePropertyMutable(property);

        final Property propertyToUpdate = encryptPasswordProperty(property);
        dirty(propertyToUpdate);
        properties.put(property.key(), propertyToUpdate);
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

    protected Property encryptPasswordProperty(Property property) {
        if (Objects.equals(propertyDefinition(property).definition().getOrDefault("password", false), true)) {
            return encrypt(property);
        }

        return property;
    }

    protected Property<String> encrypt(Property<String> passwordProperty) {
        return new MongoUserProperty<>(passwordProperty.key(), Encryption.BCRYPT.encrypt(passwordProperty.value()));
    }

    protected void ensurePropertyMutable(Property property) {
        if (Objects.equals(propertyDefinition(property).definition().getOrDefault("immutable", false), true)) {
            throw new UserValidationException(new ValidationResult(new ValidationResult.ValidateFailure(property.key(), "immutable")));
        }
    }

    protected UpdateOperations<User> generateDirtyUpdateOperations() {
        final UpdateOperations<User> operations = datastore.createUpdateOperations(User.class)
                .disableValidation();

        dirtyProperties.entrySet().stream().forEach(e ->
                operations.set("properties." + e.getKey(), e.getValue())
        );

        dirtyProperties.clear();

        return operations;
    }

    protected UserDefinition.PropertyDefinition propertyDefinition(Property property) {
        return definition.property(property.key());
    }
}
