package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.HashMap;
import java.util.Map;

public class DefaultPropertyDefinition implements UserDefinition.PropertyDefinition {
    private final Map<String, PropertyValidator> validators;
    private final String propertyPath;

    public DefaultPropertyDefinition(String propertyPath, Map<String, PropertyValidator> validators) {
        this.propertyPath = propertyPath;
        this.validators = validators;
    }

    @Override
    public Map<String, Object> definition() {
        final Map<String, Object> definition = new HashMap<>();

        for (String validatorType : validators.keySet()) {
            definition.put(validatorType, validators.get(validatorType).configuration());
        }

        return definition;
    }

    @Override
    public ValidationResult validate(Map<String, Object> user) {
        return validators.entrySet().stream()
                .map(entry -> entry.getValue().validate(user, propertyPath))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }
}
