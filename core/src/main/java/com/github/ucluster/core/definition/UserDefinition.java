package com.github.ucluster.core.definition;

import java.util.Map;

public interface UserDefinition {

    Map<String, Object> definition();

    ValidationResult validate(Map<String, Object> user);

    interface PropertyDefinition {

        String propertyPath();

        Map<String, Object> definition();

        ValidationResult validate(Map<String, Object> user);
    }
}
