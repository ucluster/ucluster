package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * EmailConcern
 * <p>
 * Take cares of email property validation.
 * For security, checks https://www.owasp.org/index.php/Input_Validation_Cheat_Sheet#Email_Address_Validation
 * <p>
 * For security concern, we have banned out the temporary email provider, like 10minutemail
 */
public class EmailConcern implements Record.Property.Concern {
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
    public boolean isAbout(Record.Property.Point point) {
        return enabled && Record.Property.Point.VALIDATE == point;
    }

    @Override
    public void effect(Record record, String path, Record.Property.Point point) {
        if (enabled) {
            final Optional<Record.Property> property = record.property(path);

            property.ifPresent(prop -> {
                final String propertyValue = String.valueOf(prop.value());

                if (!positivePattern.matcher(propertyValue).matches() || negativePattern.matcher(propertyValue).matches()) {
                    throw new ConcernEffectException(new EffectResult(new EffectResult.Failure(path, type())));
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
