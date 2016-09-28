package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

public interface PropertyProcessor<T> extends User.Property.Specification {

    boolean isAppliable(Type type);

    User.Property<T> process(User.Property<T> property);

    User.Property<T> processUpdate(User.Property<T> property);

    enum Type {
        BEFORE_CREATE,
        BEFORE_UPDATE
    }
}
