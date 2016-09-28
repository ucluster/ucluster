package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Map;

public class RequiredValidator implements PropertyValidator {
    private String type;
    private final Object configuration;

    private final boolean isRequired;

    public RequiredValidator(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.isRequired = (boolean) configuration;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ValidationResult validate(Map<String, Object> request, String propertyPath) {
        if (!isRequired) {
            return ValidationResult.SUCCESS;
        }

        if (request.get(propertyPath) != null) {
            return ValidationResult.SUCCESS;
        }

        return new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())));
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
