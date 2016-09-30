package com.github.ucluster.core.definition;

import com.github.ucluster.core.ActiveRecord;

import java.util.Map;

public interface Definition<T extends ActiveRecord> {

    ValidationResult validate(T user);

    ValidationResult validate(T user, String... propertyPaths);

    PropertyDefinition<T> property(String propertyPath);

    Map<String, Object> definition();

    interface PropertyDefinition<T extends ActiveRecord> {

        String propertyPath();

        Map<String, Object> definition();

        ValidationResult validate(T user);

        <P> ActiveRecord.Property<P> process(PropertyProcessor.Type type, ActiveRecord.Property<P> property);
    }
}
