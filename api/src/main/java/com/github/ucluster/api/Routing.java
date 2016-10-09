package com.github.ucluster.api;

import com.github.ucluster.core.User;

import java.net.URI;

import static javax.ws.rs.core.UriBuilder.fromUri;

public class Routing {
    public static URI user(User user) {
        return template("/users/{user_id}", user.uuid());
    }

    public static URI request(User user, User.Request request) {
        return template("/users/{user_id}/requests/{request_id}", user.uuid(), request.uuid());
    }

    private static URI template(String template, Object... parameters) {
        return fromUri(template).build(parameters, false);
    }
}