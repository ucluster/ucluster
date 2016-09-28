package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.inject.Injector;

import javax.inject.Inject;

public class IdentityValidator implements PropertyValidator {
    private String type;
    private Object configuration;

    private RequiredValidator requiredValidator;
    private UniquenessValidator uniquenessValidator;

    @Inject
    Injector injector;

    IdentityValidator() {
    }

    public IdentityValidator(String type, Object configuration) {
        this.type = type;
        this.configuration = configuration;
        this.requiredValidator = new RequiredValidator(type, configuration);
        this.uniquenessValidator = new UniquenessValidator(type, configuration);
    }

    @Override
    public ValidationResult validate(User user, String propertyPath) {
        injector.injectMembers(requiredValidator);
        injector.injectMembers(uniquenessValidator);

        return requiredValidator.validate(user, propertyPath)
                .merge(uniquenessValidator.validate(user, propertyPath));
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
