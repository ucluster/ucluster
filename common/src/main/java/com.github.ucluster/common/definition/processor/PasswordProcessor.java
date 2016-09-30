package com.github.ucluster.common.definition.processor;

import com.github.ucluster.core.ActiveRecord;
import com.github.ucluster.core.definition.PropertyProcessor;

public class PasswordProcessor implements PropertyProcessor {
    private String type;
    private Object configuration;
    private boolean isPassword;

    PasswordProcessor() {
    }

    public PasswordProcessor(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.isPassword = (boolean) configuration;
    }

    @Override
    public boolean isAppliable(Type type) {
        return type == Type.BEFORE_CREATE || type == Type.BEFORE_UPDATE;
    }

    @Override
    public ActiveRecord.Property process(ActiveRecord.Property property) {
        return encrypt(property);
    }

    private ActiveRecord.Property encrypt(ActiveRecord.Property property) {
        property.value(encrypt((String) property.value()));
        return property;
    }

    private String encrypt(String original) {
        return isPassword ? Encryption.BCRYPT.encrypt(original) : original;
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
