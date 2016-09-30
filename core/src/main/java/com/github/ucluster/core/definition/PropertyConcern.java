package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

public interface PropertyConcern<T> extends Record.Property.Concern {

    boolean isAppliable(Point point);

    Record.Property<T> process(Record.Property<T> property);

}
