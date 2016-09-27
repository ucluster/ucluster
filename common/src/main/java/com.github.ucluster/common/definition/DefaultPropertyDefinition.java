package com.github.ucluster.common.definition;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultPropertyDefinition implements UserDefinition.PropertyDefinition {
    private Map<String, PropertyValidator> validators = new HashMap<>();
    private final Map<String, Object> metadata;
    private final String propertyPath;

    public DefaultPropertyDefinition(String propertyPath, List<PropertyValidator> validators) {
        this(propertyPath, validators, new HashMap<>());
    }

    public DefaultPropertyDefinition(String propertyPath, List<PropertyValidator> validators, Map<String, Object> metadata) {
        this.metadata = metadata;
        this.propertyPath = propertyPath;
        validators.stream().forEach(propertyValidator -> this.validators.put(propertyValidator.type(), propertyValidator));
    }

    @Override
    public String propertyPath() {
        return propertyPath;
    }

    @Override
    public Map<String, Object> definition() {
        final Map<String, Object> definition = validators.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().configuration()
                ));

        definition.putAll(metadata);

        return definition;
    }

    @Override
    public ValidationResult validate(Map<String, Object> user) {
        return validators.entrySet().stream()
                .map(entry -> entry.getValue().validate(user, propertyPath))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }
}
