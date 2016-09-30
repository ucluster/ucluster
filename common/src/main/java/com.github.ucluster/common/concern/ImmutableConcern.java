package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Collection;
import java.util.Collections;

public class ImmutableConcern<T extends Record> implements Record.Property.Concern<T> {
    private String type;
    private Object configuration;
    private boolean enabled;

    ImmutableConcern() {
    }

    public ImmutableConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public Collection<Point> about() {
        return Collections.singletonList(Point.BEFORE_UPDATE);
    }

    @Override
    public void effect(T record, String propertyPath) {
        if (enabled) {
            throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(propertyPath, type())));
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
