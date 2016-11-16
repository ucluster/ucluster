package com.github.ucluster.core;

import java.util.HashMap;
import java.util.Map;

public class Request extends HashMap<String, Object> {
    protected Request() {
    }

    public Request(Map<String, Object> request) {
        super(request);
    }

    public static Request of(Map<String, Object> request) {
        return new Request(request);
    }

    public Map<String, String> metadata() {
        final Map<String, String> metadata = new HashMap<>((Map<String, String>) getOrDefault("metadata", new HashMap<>()));

        metadata.put("model", "user");
        if (!metadata.containsKey("type")) {
            metadata.put("type", "default");
        }
        if (!metadata.containsKey("user_type")) {
            metadata.put("user_type", metadata.get("type"));
        }

        return metadata;
    }

    public Map<String, Object> properties() {
        return (Map<String, Object>) getOrDefault("properties", new HashMap<>());
    }

    public String metadata(String key) {
        return metadata().get(key);
    }

    public Object property(String key) {
        return properties().get(key);
    }

    public Map<String, Object> request() {
        final Map<String, Object> request = new HashMap<>();
        request.put("metadata", metadata());
        request.put("properties", properties());

        return request;
    }
}
