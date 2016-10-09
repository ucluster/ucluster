package com.github.ucluster.core;

import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;

import java.util.Map;
import java.util.Optional;

public interface Repository<T extends Record> {

    Optional<T> uuid(String uuid);

    Optional<T> findBy(Record.Property property);

    PaginatedList<T> find(Criteria criteria);

    T create(Map<String, Object> request);
}
