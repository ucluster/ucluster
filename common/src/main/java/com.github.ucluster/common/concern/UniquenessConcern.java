package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

import static java.util.Arrays.asList;

public class UniquenessConcern<T extends Record> implements Record.Property.Concern<T> {
    @Inject
    Repository<User> records;

    private String type;
    private Object configuration;
    private boolean enabled;

    UniquenessConcern() {
    }

    public UniquenessConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public Collection<Point> about() {
        return asList(Point.BEFORE_CREATE, Point.BEFORE_UPDATE);
    }

    @Override
    public void effect(T record, String propertyPath) {
        if (enabled) {
            record.property(propertyPath).ifPresent(prop -> {
                final Optional<User> existingRecord = records.find(prop);

                existingRecord.ifPresent($ -> {
                    throw new ConcernEffectException(new EffectResult(asList(new EffectResult.Failure(propertyPath, type()))));
                });
            });

        }
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
