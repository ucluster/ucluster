package com.github.ucluster.common.definition;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class DefaultRecordDefinition<T extends Record> implements Definition<T> {
    private Map<String, PropertyDefinition<T>> propertyDefinitions = new HashMap<>();

    public DefaultRecordDefinition(List<PropertyDefinition<T>> propertyDefinitions) {
        propertyDefinitions.stream()
                .forEach(propertyDefinition -> this.propertyDefinitions.put(propertyDefinition.propertyPath(), propertyDefinition));
    }

    @Override
    public void effect(Record.Property.Point point, T record) {
        effect(point, record, allPaths(record));
    }

    @Override
    public void effect(Record.Property.Point point, T record, String... propertyPaths) {
        final EffectResult result = asList(propertyPaths).stream()
                .map(propertyPath -> propertyDefinitions.get(propertyPath))
                .map(propertyDefinition -> {
                    try {
                        propertyDefinition.effect(point, record);
                        return null;
                    } catch (ConcernEffectException e) {
                        return e.getEffectResult();
                    }
                })
                .filter(e -> e != null)
                .reduce(EffectResult.SUCCESS, EffectResult::merge);

        if (!result.valid()) {
            throw new ConcernEffectException(result);
        }
    }

    private String[] allPaths(T record) {
        final List<String> paths = record.properties().stream().map(Record.Property::path).collect(Collectors.toList());

        return paths.toArray(new String[paths.size()]);
    }

    @Override
    public Map<String, Object> definition() {
        return propertyDefinitions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().definition()
                ));
    }

    @Override
    public PropertyDefinition<T> property(String propertyPath) {
        return propertyDefinitions.get(propertyPath);
    }
}
