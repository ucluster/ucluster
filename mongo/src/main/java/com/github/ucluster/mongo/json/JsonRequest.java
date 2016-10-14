package com.github.ucluster.mongo.json;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.Map;

public class JsonRequest {
    private final DocumentContext context;

    public JsonRequest(DocumentContext context) {
        this.context = context;
    }

    public static JsonRequest of(Map<String, Object> request) {
        return new JsonRequest(JsonPath.parse(request));
    }

    public Object path(String path) {
        try {
            return context.read(path);
        } catch (Exception e) {
            return null;
        }
    }

    public Object property(String path) {
        return path("$.properties." + path);
    }
}
