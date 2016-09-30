package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.Collection;
import java.util.Optional;

import static java.util.Arrays.asList;

public class PasswordConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private boolean enabled;

    PasswordConcern() {
    }

    public PasswordConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public Collection<Point> about() {
        return asList(Point.BEFORE_CREATE, Point.BEFORE_UPDATE);
    }

    @Override
    public void effect(Record record, String propertyPath) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(propertyPath);

            property.ifPresent(prop -> {
                prop.value(encrypt(String.valueOf(property.get().value())));
            });
        }
    }

    private String encrypt(String original) {
        return enabled ? Encryption.BCRYPT.encrypt(original) : original;
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
