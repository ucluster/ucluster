package com.github.ucluster.core.definition;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ValidationResult {
    public static final ValidationResult SUCCESS = new ValidationResult();

    private List<ValidateFailure> errors = new ArrayList<>();

    public ValidationResult(List<ValidateFailure> errors) {
        this.errors = errors;
    }

    public ValidationResult(ValidateFailure errors) {
        this.errors = asList(errors);
    }

    ValidationResult() {
        errors = new ArrayList<>();
    }

    public ValidationResult merge(ValidationResult another) {
        List<ValidateFailure> errors = new ArrayList<>();
        errors.addAll(errors());
        errors.addAll(another.errors());
        return new ValidationResult(errors);
    }

    public List<ValidateFailure> errors() {
        return errors;
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public static class ValidateFailure {
        String path;
        String type;

        public ValidateFailure(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }
    }
}