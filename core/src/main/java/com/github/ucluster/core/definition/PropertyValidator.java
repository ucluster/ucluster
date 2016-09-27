package com.github.ucluster.core.definition;

import java.util.Map;

public interface PropertyValidator {

    String type();

    ValidationResult validate(Map<String, Object> request, String propertyPath);

    Object configuration();
}
