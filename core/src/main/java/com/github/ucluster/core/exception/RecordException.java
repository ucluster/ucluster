package com.github.ucluster.core.exception;

public class RecordException extends RuntimeException {
    private final String code;

    public RecordException(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
