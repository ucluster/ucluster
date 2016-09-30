package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.RecordValidationException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class FormatConcern implements Record.Property.Concern<User> {

    private final String type;
    private final Object configuration;
    private final Pattern pattern;

    public FormatConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.pattern = Pattern.compile((String) ((Map<String, Object>) configuration).get("pattern"));
    }

    @Override
    public Collection<Point> about() {
        return Collections.singletonList(Point.VALIDATE);
    }

    @Override
    public void effect(User record, String propertyPath) {
        record.property(propertyPath).ifPresent(prop -> {
            if (!pattern.matcher(String.valueOf(prop.value())).matches()) {
                throw new RecordValidationException(new EffectResult(asList(new EffectResult.ValidateFailure(propertyPath, type()))));
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
