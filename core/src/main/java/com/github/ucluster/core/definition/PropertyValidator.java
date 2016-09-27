package com.github.ucluster.core.definition;

import java.util.Map;

public interface PropertyValidator {

    ValidationResult validate(Map<String, Object> request);

    Object configuration();
}
