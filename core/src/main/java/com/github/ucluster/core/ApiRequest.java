package com.github.ucluster.core;

import java.util.HashMap;
import java.util.Map;

public class ApiRequest extends HashMap<String, Object> {
    protected ApiRequest() {
    }

    public ApiRequest(Map<String, Object> request) {
        super(request);
    }

    public static ApiRequest of(Map<String, Object> request) {
        return new ApiRequest(request);
    }

    public Metadata metadata() {
        return new Metadata(originalMetadata());
    }

    public Map<String, Object> properties() {
        return (Map<String, Object>) getOrDefault("properties", new HashMap<>());
    }

    public String metadata(String key) {
        return metadata().get(key);
    }

    public ApiRequest type(String value) {
        final Map<String, String> metadata = new HashMap<>(originalMetadata());
        metadata.put("type", value);
        put("metadata", metadata);

        return this;
    }

    public ApiRequest model(String value) {
        final Map<String, String> metadata = new HashMap<>(originalMetadata());
        metadata.put("model", value);
        put("metadata", metadata);

        return this;
    }

    private Map<String, String> originalMetadata() {
        return (Map<String, String>) getOrDefault("metadata", new HashMap<>());
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

    public static class Metadata extends HashMap<String, String> {
        Metadata(Map<String, String> metadata) {
            super(metadata);
            if (!containsKey("type")) {
                put("type", "default");
            }

            if (!containsKey("model")) {
                put("model", "user");
            }

            if (!containsKey("user_type")) {
                put("user_type", "default");
            }
        }

        public String type() {
            return get("type");
        }

        public String model() {
            return get("model");
        }

        public String userType() {
            return get("user_type");
        }
    }
}
