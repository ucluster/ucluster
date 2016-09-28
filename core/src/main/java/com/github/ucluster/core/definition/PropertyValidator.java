package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

import java.util.Map;

public interface PropertyValidator extends User.Property.Specification {

    ValidationResult validate(Map<String, Object> request, String propertyPath);
}
