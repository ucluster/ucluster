package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public class FormatValidator implements PropertyValidator {

    private final Object configuration;

    private final Pattern pattern;

    public FormatValidator(Object configuration) {
        this.configuration = configuration;
        this.pattern = Pattern.compile((String) ((Map<String, Object>) configuration).get("pattern"));
    }

    @Override
    public String type() {
        return "format";
    }

    @Override
    public ValidationResult validate(Map<String, Object> request, String propertyPath) {
        if (path(request, propertyPath) == null) {
            return ValidationResult.SUCCESS;
        }

        return pattern.matcher(String.valueOf(path(request, propertyPath))).matches()
                ? ValidationResult.SUCCESS
                : new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())));
    }

    @Override
    public Object configuration() {
        return configuration;
    }

    private Object path(Map<String, Object> request, String path) {
        return request.get(path);
    }
}
