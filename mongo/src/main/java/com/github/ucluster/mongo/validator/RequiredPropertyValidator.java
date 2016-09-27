package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Map;

public class RequiredPropertyValidator implements PropertyValidator {
    private final String propertyPath;
    private final Object configuration;

    private final boolean isRequired;

    public RequiredPropertyValidator(String propertyPath, Object configuration) {
        this.propertyPath = propertyPath;
        this.configuration = configuration;
        this.isRequired = (boolean) configuration;
    }

    @Override
    public ValidationResult validate(Map<String, Object> request) {
        if (!isRequired) {
            return ValidationResult.SUCCESS;
        }

        if (request.get(propertyPath) != null) {
            return ValidationResult.SUCCESS;
        }

        return new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, "required")));
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
