package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

import java.util.Map;

public interface Definition<D extends Record> {

    ValidationResult validate(D user);

    ValidationResult validate(D user, String... propertyPaths);

    PropertyDefinition<D> property(String propertyPath);

    Map<String, Object> definition();

    interface PropertyDefinition<T extends Record> {

        String propertyPath();

        Map<String, Object> definition();

        ValidationResult validate(T user);

        <P> Record.Property<P> process(PropertyProcessor.Type type, Record.Property<P> property);
    }
}
