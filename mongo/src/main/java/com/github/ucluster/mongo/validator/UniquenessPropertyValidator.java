package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class UniquenessPropertyValidator implements PropertyValidator {
    @Inject
    UserRepository users;

    private final Object configuration;
    private final boolean isUnique;

    public UniquenessPropertyValidator(Object configuration) {
        this.configuration = configuration;
        this.isUnique = (boolean) configuration;
    }

    @Override
    public String type() {
        return "uniqueness";
    }

    @Override
    public ValidationResult validate(Map<String, Object> request, String propertyPath) {
        System.out.println(request);
        final Optional<User> user = users.find(new User.Property() {
            @Override
            public String key() {
                return propertyPath;
            }

            @Override
            public String value() {
                return (String) request.get(propertyPath);
            }
        });

        return user.isPresent()
                ? new ValidationResult(Arrays.asList(new ValidationResult.ValidateFailure(propertyPath, type())))
                : ValidationResult.SUCCESS;
    }

    @Override
    public Object configuration() {
        return configuration;
    }
}
