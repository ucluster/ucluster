package com.github.ucluster.common.definition;

import com.github.ucluster.core.definition.ValidationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultUserDefinition implements com.github.ucluster.core.definition.UserDefinition {
    private Map<String, PropertyDefinition> propertyDefinitions = new HashMap<>();

    public DefaultUserDefinition(List<PropertyDefinition> propertyDefinitions) {
        propertyDefinitions.stream().forEach(propertyDefinition -> this.propertyDefinitions.put(propertyDefinition.propertyPath(), propertyDefinition));
    }

    @Override
    public Map<String, Object> definition() {
        final Map<String, Object> definition = new HashMap<>();

        for (String propertyPath : propertyDefinitions.keySet()) {
            definition.put(propertyPath, propertyDefinitions.get(propertyPath).definition());
        }

        return definition;
    }

    public ValidationResult validate(Map<String, Object> request) {
        return propertyDefinitions.values().stream()
                .map(fv -> fv.validate(request))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }

    @Override
    public PropertyDefinition property(String key) {
        return propertyDefinitions.get(key);
    }
}
