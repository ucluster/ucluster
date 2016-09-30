package com.github.ucluster.core.exception;

import com.github.ucluster.core.definition.EffectResult;

public class ConcernEffectException extends RuntimeException {
    private final EffectResult effectResult;

    public ConcernEffectException(EffectResult effectResult) {
        this.effectResult = effectResult;
    }

    public EffectResult getEffectResult() {
        return effectResult;
    }

    @Override
    public String getMessage() {
        return getEffectResult().errors().stream()
                .map(error -> "path: " + error.getPropertyPath() + ", type: " + error.getType())
                .reduce("user validation failed by:", (a, b) -> a + "\n" + b);
    }
}
