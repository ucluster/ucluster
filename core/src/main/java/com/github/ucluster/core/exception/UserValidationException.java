package com.github.ucluster.core.exception;

import com.github.ucluster.core.definition.ValidationResult;

public class UserValidationException extends RuntimeException {
    private final ValidationResult validationResult;

    public UserValidationException(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
