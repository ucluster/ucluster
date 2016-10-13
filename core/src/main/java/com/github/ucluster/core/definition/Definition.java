package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

import java.util.Collection;
import java.util.Map;

public interface Definition<D extends Record> {

    void effect(Record.Property.Point point, D record);

    void effect(Record.Property.Point point, D record, String... propertyPaths);

    PropertyDefinition<D> property(String propertyPath);

    Collection<PropertyDefinition<D>> properties();

    Map<String, Object> definition();

    interface PropertyDefinition<T extends Record> {

        String path();

        Map<String, Object> definition();

        void effect(Record.Property.Point point, T record);
    }
}
