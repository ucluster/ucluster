package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.Optional;

public class HiddenConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private boolean enabled;

    HiddenConcern() {

    }

    HiddenConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return enabled && point == Record.Property.Point.DELIVERY;
    }

    @Override
    public void effect(Record record, String propertyPath, Record.Property.Point point) {
        final Optional<Record.Property> property = record.property(propertyPath);

        property.ifPresent(prop -> {
            prop.value(null);
        });
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
