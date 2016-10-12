package com.github.ucluster.core.exception;

public class RecordTypeNotSupportedException extends RuntimeException {
    private String type;

    public RecordTypeNotSupportedException(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
