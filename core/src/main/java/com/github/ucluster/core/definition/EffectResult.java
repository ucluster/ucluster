package com.github.ucluster.core.definition;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class EffectResult {
    public static final EffectResult SUCCESS = new EffectResult();

    private List<ValidateFailure> errors = new ArrayList<>();

    public EffectResult(List<ValidateFailure> errors) {
        this.errors = errors;
    }

    public EffectResult(ValidateFailure errors) {
        this.errors = asList(errors);
    }

    EffectResult() {
        errors = new ArrayList<>();
    }

    public EffectResult merge(EffectResult another) {
        List<ValidateFailure> errors = new ArrayList<>();
        errors.addAll(errors());
        errors.addAll(another.errors());
        return new EffectResult(errors);
    }

    public List<ValidateFailure> errors() {
        return errors;
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public static class ValidateFailure {
        String propertyPath;
        String type;

        public ValidateFailure(String propertyPath, String type) {
            this.propertyPath = propertyPath;
            this.type = type;
        }

        public String getPropertyPath() {
            return propertyPath;
        }

        public String getType() {
            return type;
        }
    }
}