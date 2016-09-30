package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

import java.util.Map;

public interface Definition<T extends Record> {

    ValidationResult validate(T user);

    ValidationResult validate(T user, String... propertyPaths);

    PropertyDefinition<T> property(String propertyPath);

    Map<String, Object> definition();

    interface PropertyDefinition<T extends Record> {

        String propertyPath();

        Map<String, Object> definition();

        ValidationResult validate(T user);

        <P> Record.Property<P> process(PropertyProcessor.Type type, Record.Property<P> property);
    }
}
