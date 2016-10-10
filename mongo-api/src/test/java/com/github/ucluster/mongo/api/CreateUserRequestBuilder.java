package com.github.ucluster.mongo.api;

import java.util.HashMap;
import java.util.Map;

public class CreateUserRequestBuilder {
    private String type = "register";
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    public CreateUserRequestBuilder type(String type) {
        this.type = type;
        return this;
    }

    public CreateUserRequestBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public CreateUserRequestBuilder properties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public Map<String, Object> get() {
        final Map<String, Object> request = new HashMap<>();

        metadata.put("recordGroup", type);
        request.put("metadata", metadata);
        request.put("properties", properties);

        return request;
    }

    public static CreateUserRequestBuilder of(String type) {
        final CreateUserRequestBuilder createUserRequestBuilder = new CreateUserRequestBuilder();
        createUserRequestBuilder.type = type;
        return createUserRequestBuilder;
    }
}
