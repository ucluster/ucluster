package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Optional;

public class RequiredValidator implements PropertyValidator<User> {
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
    public ValidationResult validate(User user, String propertyPath) {
        if (!isRequired) {
            return ValidationResult.SUCCESS;
        }

        final Optional<Record.Property> property = user.property(propertyPath);
        if (property.isPresent()) {
            return ValidationResult.SUCCESS;
        }

        return new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())));
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
