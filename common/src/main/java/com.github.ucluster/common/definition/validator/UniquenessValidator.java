package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

public class UniquenessValidator implements PropertyValidator {
    @Inject
    Repository<User> users;

    private String type;
    private final Object configuration;
    private final boolean isUnique;

    public UniquenessValidator(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.isUnique = (boolean) configuration;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ValidationResult validate(User user, String propertyPath) {
        final Optional<Record.Property> property = user.property(propertyPath);

        if (!property.isPresent()) {
            return ValidationResult.SUCCESS;
        }

        if (isUnique) {
            final Optional<User> existingUser = users.find(property.get());

            return existingUser.isPresent()
                    ? new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())))
                    : ValidationResult.SUCCESS;
        }

        return ValidationResult.SUCCESS;
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
