package com.github.ucluster.test.framework.json;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import javax.ws.rs.core.Response;

public class JsonContext {
    private DocumentContext context;

    public JsonContext(DocumentContext context) {
        this.context = context;
    }

    public Object path(String path) {
        try {
            return context.read(path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    public static JsonContext json(Object object) {
        final DocumentContext context = JsonPath.parse(object);
        return new JsonContext(context);
    }

    public static JsonContext json(Response response) {
        final DocumentContext context = JsonPath.parse(response.readEntity(String.class));
        return new JsonContext(context);
    }
}