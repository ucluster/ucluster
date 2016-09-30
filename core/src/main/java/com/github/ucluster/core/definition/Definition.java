package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

import java.util.Map;

public interface Definition<D extends Record> {

    void effect(Record.Property.Point point, D record);

    void effect(Record.Property.Point point, D record, String... propertyPaths);

    PropertyDefinition<D> property(String propertyPath);

    Map<String, Object> definition();

    interface PropertyDefinition<T extends Record> {

        String propertyPath();

        Map<String, Object> definition();

        void effect(Record.Property.Point point, T record);
    }
}
