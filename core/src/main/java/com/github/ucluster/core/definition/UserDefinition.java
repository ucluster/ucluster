package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

import java.util.Map;

public interface UserDefinition {

    Map<String, Object> definition();

    ValidationResult validate(User user);

    ValidationResult validate(User user, String... propertyPaths);

    PropertyDefinition property(String propertyPath);

    interface PropertyDefinition {

        String propertyPath();

        Map<String, Object> definition();

        ValidationResult validate(User user);

        <T> User.Property<T> process(PropertyProcessor.Type type, User.Property<T> property);
    }
}
