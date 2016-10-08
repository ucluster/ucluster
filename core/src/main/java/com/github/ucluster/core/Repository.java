package com.github.ucluster.core;

import java.util.Map;
import java.util.Optional;

public interface Repository<T> {

    Optional<T> uuid(String uuid);

    Optional<T> find(Record.Property property);

    T create(Map<String, Object> request);
}
