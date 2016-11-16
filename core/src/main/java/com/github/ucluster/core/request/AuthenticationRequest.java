package com.github.ucluster.core.request;

import com.github.ucluster.core.Request;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationRequest extends Request {
    public static AuthenticationRequest of(Map<String, Object> request) {
        return new AuthenticationRequest(request);
    }

    AuthenticationRequest() {
        super();
    }

    private AuthenticationRequest(Map<String, Object> request) {
        super(request);
    }

    @Override
    public Map<String, String> metadata() {
        final Map<String, String> metadata = new HashMap<>(super.metadata());

        if (!metadata.containsKey("user_type")) {
            metadata.put("user_type", "default");
        }
        if (!metadata.containsKey("method")) {
            metadata.put("method", "password");
        }

        return metadata;
    }
}
