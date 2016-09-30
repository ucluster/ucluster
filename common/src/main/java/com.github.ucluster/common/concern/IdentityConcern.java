package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.inject.Injector;

import javax.inject.Inject;

public class IdentityConcern implements Record.Property.Concern {
    private String type;
    private Object configuration;

    private RequiredConcern requiredConcern;
    private UniquenessConcern uniquenessConcern;

    @Inject
    Injector injector;

    IdentityConcern() {
    }

    public IdentityConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.requiredConcern = new RequiredConcern(type, configuration);
        this.uniquenessConcern = new UniquenessConcern(type, configuration);
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.BEFORE_CREATE == point || Record.Property.Point.BEFORE_UPDATE == point;
    }

    @Override
    public void effect(Record record, String propertyPath) {
        injector.injectMembers(requiredConcern);
        injector.injectMembers(uniquenessConcern);

        EffectResult result = EffectResult.SUCCESS;

        try {
            requiredConcern.effect(record, propertyPath);
        } catch (ConcernEffectException e) {
            result = result.merge(e.getEffectResult());
        }

        try {
            uniquenessConcern.effect(record, propertyPath);
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
