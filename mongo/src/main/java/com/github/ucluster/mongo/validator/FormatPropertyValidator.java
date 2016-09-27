package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public class FormatPropertyValidator implements PropertyValidator {

    private final String propertyPath;
    private final Pattern pattern;
    private final Object configuration;

    public FormatPropertyValidator(String propertyPath, Object configuration) {
        this.propertyPath = propertyPath;
        this.configuration = configuration;
        this.pattern = Pattern.compile((String) ((Map<String, Object>) configuration).get("pattern"));
    }

    @Override
    public ValidationResult validate(Map<String, Object> request) {
        if (path(request, propertyPath) == null) {
            return ValidationResult.SUCCESS;
        }

        return pattern.matcher(String.valueOf(path(request, propertyPath))).matches()
                ? ValidationResult.SUCCESS
                : new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, "format")));
    }

    @Override
    public Object configuration() {
        return configuration;
    }

    private Object path(Map<String, Object> request, String path) {
        return request.get(path);
    }
}
