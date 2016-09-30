package com.github.ucluster.common.request;

import com.github.ucluster.core.Record;

import java.util.HashMap;
import java.util.Map;

public class DefaultRequest implements Record.Request {
    private final Map<String, Object> request;

    DefaultRequest(Map<String, Object> request) {
        this.request = request;
    }

    @Override
    public String type() {
        return (String) metadata().getOrDefault("type", "register");
    }

    @Override
    public Map<String, Object> metadata() {
        final Map<String, Object> defaultMetadata = new HashMap<>();
        defaultMetadata.put("user_type", "default");

        return (Map<String, Object>) request.getOrDefault("metadata", defaultMetadata);
    }

    @Override
    public Map<String, Object> properties() {
        return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
    }

    @Override
    public Map<String, Object> request() {
        return request;
    }

}
