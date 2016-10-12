package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.Optional;

public class TransientConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private boolean enabled;

    TransientConcern() {
    }

    public TransientConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return enabled && (point == Record.Property.Point.BEFORE_CREATE || point == Record.Property.Point.BEFORE_UPDATE);
    }

    @Override
    public void effect(Record record, String propertyPath) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(propertyPath);

            property.ifPresent(prop -> {
                //mark the value as null, then the record will not save this property
                prop.value(null);
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
