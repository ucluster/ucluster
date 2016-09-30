package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import static java.util.Arrays.asList;

public class RequiredConcern implements Record.Property.Concern {
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
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.VALIDATE == point;
    }

    @Override
    public void effect(Record record, String propertyPath) {
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
