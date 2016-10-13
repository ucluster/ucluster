package com.github.ucluster.common.definition;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultPropertyDefinition<T extends Record> implements Definition.PropertyDefinition<T> {
    private final String propertyPath;
    private Map<String, Record.Property.Concern> concerns = new HashMap<>();

    public DefaultPropertyDefinition(String propertyPath, Collection<Record.Property.Concern> concerns) {
        this.propertyPath = propertyPath;
        concerns.stream().forEach(concern -> this.concerns.put(concern.type(), concern));
    }

    @Override
    public void effect(Record.Property.Point point, T record) {
        final EffectResult result = concerns.values().stream()
                .filter(concern -> concern.isAbout(point))
                .map(concern -> {
                    try {
                        concern.effect(record, propertyPath, point);
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

    @Override
    public String path() {
        return propertyPath;
    }

    @Override
    public Map<String, Object> definition() {
        return concerns.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().configuration()
                ));
    }
}
