package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import javax.inject.Inject;
import java.util.Optional;

/**
 * UniquenessConcern:
 * <p>
 * needs database constraint support.
 * <p>
 * TODO: enable different Record implementation exists at the same time
 */
public class UniquenessConcern implements Record.Property.Concern {
    @Inject
    Injector injector;

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
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.BEFORE_CREATE == point || Record.Property.Point.BEFORE_UPDATE == point;
    }

    @Override
    public void effect(Record record, String propertyPath) {
        if (enabled) {
            record.property(propertyPath).ifPresent(prop -> {
                final Repository<? extends Record> records = injector.getInstance(Key.get(new TypeLiteral<Repository<? extends Record>>() {
                }));

                final Optional<? extends Record> existingRecord = records.find(prop);

                existingRecord.ifPresent($ -> {
                    throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(propertyPath, type())));
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
