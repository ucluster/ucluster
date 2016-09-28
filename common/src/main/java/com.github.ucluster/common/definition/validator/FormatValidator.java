package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class FormatValidator implements PropertyValidator {

    private final String type;
    private final Object configuration;
    private final Pattern pattern;

    public FormatValidator(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.pattern = Pattern.compile((String) ((Map<String, Object>) configuration).get("pattern"));
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ValidationResult validate(User user, String propertyPath) {
        final Optional<User.Property> property = user.property(propertyPath);

        if (!property.isPresent()) {
            return ValidationResult.SUCCESS;
        }

        return pattern.matcher(String.valueOf(property.get().value())).matches()
                ? ValidationResult.SUCCESS
                : new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())));
    }

    @Override
    public Object configuration() {
        return configuration;
    }

}
