package com.github.ucluster.common.definition;

import com.github.ucluster.core.authentication.Authentication;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class DefaultRecordDefinition<T extends Record> implements Definition<T> {
    private Map<String, PropertyDefinition<T>> propertyDefinitions = new HashMap<>();

    public DefaultRecordDefinition(List<PropertyDefinition<T>> propertyDefinitions) {
        propertyDefinitions.stream()
                .forEach(propertyDefinition -> this.propertyDefinitions.put(propertyDefinition.path(), propertyDefinition));
    }

    @Override
    public void effect(Record.Property.Point point, T record) {
        effect(point, record, propertyDefinitions.keySet().stream().toArray(String[]::new));
    }

    @Override
    public void effect(Record.Property.Point point, T record, String... paths) {
        EffectResult result =
                effectNonUndefinedPropertyExist(record, paths)
                        .merge(effectOnPropertyDefinition(point, record, paths));

        if (!result.valid()) {
            throw new ConcernEffectException(result);
        }
    }

    @Override
    public void merge(Definition<T> definition) {
        definition.properties().forEach(property -> {
            propertyDefinitions.put(property.path(), property);
        });
    }

    private EffectResult effectOnPropertyDefinition(Record.Property.Point point, T record, String... paths) {
        return asList(paths).stream()
                .filter(path -> {
                    //TODO: refactor
                    if (record instanceof User.Request && path.equals("status")) {
                        return false;
                    }

                    if (record instanceof Authentication) {
                        return false;
                    }

                    return true;
                })
                .map(path -> propertyDefinitions.get(path))
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
    public PropertyDefinition<T> property(String path) {
        return propertyDefinitions.get(path);
    }

    @Override
    public Collection<PropertyDefinition<T>> properties() {
        return propertyDefinitions.values();
    }

    protected EffectResult effectNonUndefinedPropertyExist(T record, String... propertyPaths) {
        EffectResult result = EffectResult.SUCCESS;

        for (String path : mergePaths(record, propertyPaths)) {
            //TODO: for request to store status
            if (record instanceof User.Request && path.equals("status")) {
                continue;
            }

            if (record instanceof Authentication) {
                continue;
            }

            if (!propertyDefinitions.containsKey(path)) {
                result = result.merge(new EffectResult(new EffectResult.Failure(path, "undefined")));
            }
        }

        return result;
    }

    private Set<String> mergePaths(T record, String... propertyPaths) {
        final Set<String> paths = new HashSet<>(asList(propertyPaths));
        paths.addAll(record.properties().stream().map(Record.Property::path).collect(Collectors.toList()));
        return paths;
    }
}
