package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;

import java.util.Optional;

public class CredentialConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private boolean enabled;

    CredentialConcern() {
    }

    public CredentialConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return enabled && (Record.Property.Point.BEFORE_CREATE == point || Record.Property.Point.BEFORE_UPDATE == point || Record.Property.Point.DELIVERY == point);
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(path);
            if (point != Record.Property.Point.DELIVERY) {
                property.ifPresent(prop -> {
                    prop.value(encrypt(String.valueOf(property.get().value())));
                });
            } else {
                property.ifPresent(prop -> {
                    prop.value(null);
                });
            }
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
