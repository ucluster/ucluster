package com.github.ucluster.core.definition;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class EffectResult {
    public static final EffectResult SUCCESS = new EffectResult();

    private List<Failure> errors = new ArrayList<>();

    public EffectResult(List<Failure> errors) {
        this.errors = errors;
    }

    public EffectResult(Failure errors) {
        this.errors = asList(errors);
    }

    EffectResult() {
        errors = new ArrayList<>();
    }

    public EffectResult merge(EffectResult another) {
        List<Failure> errors = new ArrayList<>();
        errors.addAll(errors());
        errors.addAll(another.errors());
        return new EffectResult(errors);
    }

    public List<Failure> errors() {
        return errors;
    }

    public boolean valid() {
        return errors.isEmpty();
    }

    public static class Failure {
        String propertyPath;
        String type;

        public Failure(String propertyPath, String type) {
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