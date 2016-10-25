package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

public class NumericConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private boolean enabled;

    NumericConcern() {
    }

    NumericConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return enabled && Record.Property.Point.VALIDATE == point;
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(path);

            property.ifPresent(prop -> {
                final String propertyValue = String.valueOf(prop.value());

                if (!NumberUtils.isNumber(propertyValue)) {
                    throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(path, type())));
                }
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
