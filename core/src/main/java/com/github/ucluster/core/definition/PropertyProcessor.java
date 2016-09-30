package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

public interface PropertyProcessor<T> extends Record.Property.Specification {

    boolean isAppliable(Type type);

    Record.Property<T> process(Record.Property<T> property);

    enum Type {
        BEFORE_CREATE,
        BEFORE_UPDATE
    }
}
