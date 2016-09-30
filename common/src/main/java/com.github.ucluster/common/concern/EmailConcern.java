package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.RecordValidationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmailConcern implements Record.Property.Concern<User> {
    private String type;
    private Object configuration;
    private boolean enabled;
    private static Pattern positivePattern = Pattern.compile("[a-z0-9!#$%&'*+\"=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+\"=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
    private static Pattern negativePattern = Pattern.compile(".*@10minutemail\\.com|.*@dreggn\\.com");

    EmailConcern() {
    }

    public EmailConcern(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.enabled = (boolean) configuration;
    }

    @Override
    public Collection<Point> about() {
        return Collections.singletonList(Point.VALIDATE);
    }

    @Override
    public void effect(User record, String propertyPath) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(propertyPath);

            property.ifPresent(prop -> {
                final String propertyValue = String.valueOf(prop.value());

                if (!positivePattern.matcher(propertyValue).matches() || negativePattern.matcher(propertyValue).matches()) {
                    throw new RecordValidationException(new EffectResult(Arrays.asList(new EffectResult.Failure(propertyPath, type()))));
                }
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
