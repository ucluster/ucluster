package com.github.ucluster.common.request;

import com.github.ucluster.core.ActiveRecord;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    private String type = "register";
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    public RequestBuilder type(String type) {
        this.type = type;
        return this;
    }

    public RequestBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public RequestBuilder properties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public ActiveRecord.Request get() {
        final Map<String, Object> request = new HashMap<>();

        metadata.put("type", type);
        request.put("metadata", metadata);
        request.put("properties", properties);

        return new DefaultRequest(request);
    }

    public static RequestBuilder of(String type) {
        final RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.type = type;
        return requestBuilder;
    }
}
