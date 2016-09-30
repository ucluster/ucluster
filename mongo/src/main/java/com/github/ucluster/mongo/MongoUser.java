package com.github.ucluster.mongo;

import com.github.ucluster.common.definition.DefaultUserDefinition;
import com.github.ucluster.common.definition.processor.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.exception.UserAuthenticationException;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Entity("users")
@Converters(JodaDateTimeConverter.class)
public class MongoUser extends MongoRecord implements User {

    @org.mongodb.morphia.annotations.Property
    protected Map<String, Object> metadata;

    @Transient
    protected Definition<User> definition;

    @Override
    public void authenticate(Property identityProperty, Property passwordProperty) {
        ensureIdentityPropertyMatches(identityProperty);
        ensurePasswordPropertyMatches(passwordProperty);
    }

    private void ensurePasswordPropertyMatches(Property property) {
        final Optional<Property> passwordProperty = property(property.path());

        if (!passwordProperty.isPresent()) {
            throw new UserAuthenticationException();
        }

        if (!isPropertyPassword(definition.property(property.path()))) {
            throw new UserAuthenticationException();
        }

        if (!Encryption.BCRYPT.check((String) property.value(), (String) passwordProperty.get().value())) {
            throw new UserAuthenticationException();
        }
    }

    private void ensureIdentityPropertyMatches(Property property) {
        final Optional<Property> identityProperty = property(property.path());

        if (!identityProperty.isPresent()) {
            throw new UserAuthenticationException();
        }

        if (!isPropertyIdentity(definition.property(property.path()))) {
            throw new UserAuthenticationException();
        }

        if (!identityProperty.get().value().equals(property.value())) {
            throw new UserAuthenticationException();
        }
    }

    private boolean isPropertyPassword(DefaultUserDefinition.PropertyDefinition propertyDefinition) {
        return Objects.equals(propertyDefinition.definition().get("password"), true);
    }

    private boolean isPropertyIdentity(DefaultUserDefinition.PropertyDefinition propertyDefinition) {
        return Objects.equals(propertyDefinition.definition().get("identity"), true);
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}
