package com.github.ucluster.test.framework.request;

import com.github.ucluster.core.ApiRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    protected String type;
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

    protected Map<String, Object> get() {
        final Map<String, Object> request = new HashMap<>();

        if (type != null) {
            metadata.put("type", type);
        }
        request.put("metadata", metadata);
        request.put("properties", properties);

        return request;
    }

    public ApiRequest request() {
        return ApiRequest.of(get());
    }

    public static RequestBuilder of(String type) {
        final RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.type = type;
        return requestBuilder;
    }

    public static RequestBuilder of() {
        return new RequestBuilder();
    }
}
