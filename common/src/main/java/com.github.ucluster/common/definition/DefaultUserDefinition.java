package com.github.ucluster.common.definition;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultUserDefinition implements UserDefinition {
    private Map<String, PropertyDefinition> propertyDefinitions = new HashMap<>();

    public DefaultUserDefinition(List<PropertyDefinition> propertyDefinitions) {
        propertyDefinitions.stream().forEach(propertyDefinition -> this.propertyDefinitions.put(propertyDefinition.propertyPath(), propertyDefinition));
    }

    @Override
    public Map<String, Object> definition() {
        return propertyDefinitions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().definition()
                ));
    }

    public ValidationResult validate(User user) {
        return propertyDefinitions.values().stream()
                .map(propertyDefinition -> propertyDefinition.validate(user))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }

    @Override
    public PropertyDefinition property(String propertyPath) {
        return propertyDefinitions.get(propertyPath);
    }
}
