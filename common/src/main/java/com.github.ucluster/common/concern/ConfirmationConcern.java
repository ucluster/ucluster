package com.github.ucluster.common.concern;

import com.github.ucluster.confirmation.ConfirmationException;
import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import javax.inject.Inject;

import static com.github.ucluster.core.Record.Property.Point.BEFORE_CREATE;
import static com.github.ucluster.core.Record.Property.Point.BEFORE_UPDATE;

public class ConfirmationConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;
    private String method;

    @Inject
    ConfirmationRegistry registry;

    @Override
    public String type() {
        return type;
    }

    public ConfirmationConcern() {
    }

    public ConfirmationConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.method = (String) configuration;
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return BEFORE_CREATE == point || BEFORE_UPDATE == point;
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        try {
            String token = (String) record.metadata("token").orElseThrow(ConfirmationException::new);
            Record.Property property = record.property(path).orElseThrow(ConfirmationException::new);
            registry.find(method).ifPresent(service -> service.confirm((String) property.value(), token));
        } catch (ConfirmationException e) {
            throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(path, type())));
        }
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
