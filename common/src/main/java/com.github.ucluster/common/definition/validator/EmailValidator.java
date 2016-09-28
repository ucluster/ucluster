package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmailValidator implements PropertyValidator {
    private String type;
    private Object configuration;
    private boolean isEmail;
    private static Pattern positivePattern = Pattern.compile("[a-z0-9!#$%&'*+\"=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+\"=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
    private static Pattern negativePattern = Pattern.compile(".*@10minutemail\\.com|.*@dreggn\\.com");

    EmailValidator() {

    }

    public EmailValidator(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.isEmail = (boolean) configuration;
    }

    @Override
    public ValidationResult validate(User user, String propertyPath) {
        if (!isEmail) {
            return ValidationResult.SUCCESS;
        }

        final Optional<User.Property> property = user.property(propertyPath);

        if (!property.isPresent()) {
            return ValidationResult.SUCCESS;
        }

        final String propertyValue = String.valueOf(property.get().value());
        return positivePattern.matcher(propertyValue).matches() && !negativePattern.matcher(propertyValue).matches()
                ? ValidationResult.SUCCESS
                : new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())));
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
