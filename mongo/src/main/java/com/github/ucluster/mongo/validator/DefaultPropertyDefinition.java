package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.HashMap;
import java.util.Map;

public class DefaultPropertyDefinition implements UserDefinition.PropertyDefinition {
    private final Map<String, PropertyValidator> validators;

    public DefaultPropertyDefinition(Map<String, PropertyValidator> validators) {
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
                .map(entry -> entry.getValue().validate(user))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }
}
