package com.github.ucluster.core;

import java.util.Optional;

public interface Repository<T> {

    Optional<T> uuid(String uuid);

    Optional<T> find(ActiveRecord.Property property);

    T create(ActiveRecord.Request request);
}
