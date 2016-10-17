package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.inject.Injector;

import javax.inject.Inject;

public class IdentityConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;

    private UniquenessConcern uniquenessConcern;

    @Inject
    Injector injector;

    IdentityConcern() {
    }

    public IdentityConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.uniquenessConcern = new UniquenessConcern(type, configuration);
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.BEFORE_CREATE == point || Record.Property.Point.BEFORE_UPDATE == point;
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        injector.injectMembers(uniquenessConcern);

        EffectResult result = EffectResult.SUCCESS;

        try {
            uniquenessConcern.effect(record, path, point);
        } catch (ConcernEffectException e) {
            result = result.merge(e.getEffectResult());
        }

        if (!result.valid()) {
            throw new ConcernEffectException(result);
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
