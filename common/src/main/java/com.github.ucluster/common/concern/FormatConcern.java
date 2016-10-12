package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Map;
import java.util.regex.Pattern;

public class FormatConcern implements Record.Property.Concern {

    private String type;
    private Object configuration;
    private Pattern pattern;

    FormatConcern() {
    }

    public FormatConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.pattern = Pattern.compile((String) ((Map<String, Object>) configuration).get("pattern"));
    }

    @Override
    public boolean isAbout(Record.Property.Point point) {
        return Record.Property.Point.VALIDATE == point;
    }

    @Override
    public void effect(Record record, String propertyPath, Record.Property.Point point) {
        record.property(propertyPath).ifPresent(prop -> {
            if (!pattern.matcher(String.valueOf(prop.value())).matches()) {
                throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(propertyPath, type())));
            }
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
