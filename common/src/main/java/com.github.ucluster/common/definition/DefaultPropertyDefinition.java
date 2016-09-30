package com.github.ucluster.common.definition;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultPropertyDefinition implements Definition.PropertyDefinition<User> {
    private Map<String, PropertyValidator> validators = new HashMap<>();
    private Map<String, PropertyProcessor> processors = new HashMap<>();

    private final String propertyPath;

    public DefaultPropertyDefinition(String propertyPath, List<PropertyValidator> validators) {
        this(propertyPath, validators, Lists.newArrayList());
    }

    public DefaultPropertyDefinition(String propertyPath, List<PropertyValidator> validators, List<PropertyProcessor> processors) {
        this.propertyPath = propertyPath;
        processors.stream().forEach(propertyProcessor -> this.processors.put(propertyProcessor.type(), propertyProcessor));
        validators.stream().forEach(propertyValidator -> this.validators.put(propertyValidator.type(), propertyValidator));
    }

    @Override
    public String propertyPath() {
        return propertyPath;
    }

    @Override
    public Map<String, Object> definition() {
        final Map<String, Object> definition = validatorDefinitions();

        definition.putAll(processorDefinitions());

        return definition;
    }

    private Map<String, Object> validatorDefinitions() {
        return validators.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().configuration()
                ));
    }

    private Map<String, Object> processorDefinitions() {
        return processors.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().configuration()
                ));
    }

    @Override
    public ValidationResult validate(User user) {
        return validators.entrySet().stream()
                .map(entry -> entry.getValue().validate(user, propertyPath))
                .reduce(ValidationResult.SUCCESS, ValidationResult::merge);
    }

    @Override
    public <T> Record.Property<T> process(PropertyProcessor.Type type, Record.Property<T> property) {
        Record.Property<T> result = property;

        for (PropertyProcessor processor : processors.values()) {
            if (processor.isAppliable(type)) {
                result = processor.process(property);
            }
        }

        return result;
    }

}
