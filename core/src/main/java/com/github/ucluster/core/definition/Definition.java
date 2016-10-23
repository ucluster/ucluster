package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

import java.util.Collection;
import java.util.Map;

public interface Definition<D extends Record> {

    void effect(Record.Property.Point point, D record);

    void effect(Record.Property.Point point, D record, String... paths);

    PropertyDefinition<D> property(String path);

    Collection<PropertyDefinition<D>> properties();

    Map<String, Object> definition();

    Collection<Verification> verifications();

    interface PropertyDefinition<T extends Record> {

        String path();

        Map<String, Object> definition();

        void effect(Record.Property.Point point, T record);
    }

    interface Verification {

        String target();

        String method();
    }
}
