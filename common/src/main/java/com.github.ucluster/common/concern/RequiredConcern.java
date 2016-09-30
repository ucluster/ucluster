package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

public class RequiredConcern<T extends Record> implements Record.Property.Concern<T> {
    private String type;
    private Object configuration;
    private boolean enabled;

    RequiredConcern() {
    }

    public RequiredConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public Collection<Point> about() {
        return Collections.singletonList(Point.VALIDATE);
    }

    @Override
    public void effect(T record, String propertyPath) {
        if (enabled) {
            record.property(propertyPath).orElseThrow(() ->
                    new ConcernEffectException(
                            new EffectResult(
                                    asList(new EffectResult.Failure(propertyPath, type()))
                            )
                    )
            );
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
