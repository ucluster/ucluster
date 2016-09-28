package com.github.ucluster.common.definition.processor;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;

public class ImmutableProcessor implements PropertyProcessor {
    private String type;
    private Object configuration;
    private boolean isImmutable;

    ImmutableProcessor() {

    }

    public ImmutableProcessor(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.isImmutable = (boolean) configuration;
    }

    @Override
    public boolean isAppliable(Type type) {
        return type == Type.BEFORE_UPDATE;
    }

    @Override
    public User.Property process(User.Property property) {
        if (isImmutable) {
            throw new UserValidationException(new ValidationResult(new ValidationResult.ValidateFailure(property.key(), "immutable")));
        }

        return property;
    }

    @Override
    public User.Property processUpdate(User.Property property) {
        if (isImmutable) {
            throw new UserValidationException(new ValidationResult(new ValidationResult.ValidateFailure(property.key(), "immutable")));
        }

        return property;
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
