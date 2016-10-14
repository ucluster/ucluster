package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;

import java.util.Map;

public class NonAutoApprovableRequest extends AutoApprovableRequest {
    NonAutoApprovableRequest() {
        super();
    }

    public NonAutoApprovableRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean auto() {
        return false;
    }

}
