package com.github.ucluster.core.exception;

import com.github.ucluster.core.User;

import java.util.Map;

public class RequestTypeNotSupportException extends RequestException {
    protected User user;
    protected Map<String, Object> request;

    public RequestTypeNotSupportException(User user, Map<String, Object> request) {
        this.user = user;
        this.request = request;
    }

    public User getUser() {
        return user;
    }

    public Map<String, Object> getRequest() {
        return request;
    }
}
