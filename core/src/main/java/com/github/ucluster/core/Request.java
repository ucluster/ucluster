package com.github.ucluster.core;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final Map<String, Object> request;

    public Request(Map<String, Object> request) {
        this.request = request;
    }

    public Map<String, String> metadata() {
        return (Map<String, String>) request.getOrDefault("metadata", new HashMap<>());
    }

    public Map<String, Object> properties() {
        return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
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
