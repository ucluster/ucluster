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
        public Metadata(Map<String, String> metadata) {
            super(metadata);
        }

        public String type() {
            return getOrDefault("type", "default");
        }

        public String model() {
            return getOrDefault("model", "user");
        }

        public String userType() {
            return getOrDefault("user_type", "default");
        }

        public Map<String, String> metadata() {
            if (!containsKey("type")) {
                put("type", type());
            }

            if (!containsKey("model")) {
                put("model", model());
            }

            if (!containsKey("user_type")) {
                put("user_type", userType());
            }

            return this;
        }
    }
}
