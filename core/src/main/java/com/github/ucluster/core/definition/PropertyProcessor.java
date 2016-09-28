package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

public interface PropertyProcessor<T> extends User.Property.Specification {

    User.Property<T> processSave(User.Property<T> property);

    User.Property<T> processUpdate(User.Property<T> property);
}
