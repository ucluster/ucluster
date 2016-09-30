package com.github.ucluster.core.definition;

import com.github.ucluster.core.ActiveRecord;

public interface PropertyProcessor<T> extends ActiveRecord.Property.Specification {

    boolean isAppliable(Type type);

    ActiveRecord.Property<T> process(ActiveRecord.Property<T> property);

    enum Type {
        BEFORE_CREATE,
        BEFORE_UPDATE
    }
}
