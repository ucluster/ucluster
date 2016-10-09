package com.github.ucluster.api;

import com.github.ucluster.core.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class UserResource {
    private final User user;

    public UserResource(User user) {
        this.user = user;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User get() {
        return user;
    }

    @Path("requests")
    public RequestsResource requests() {
        return new RequestsResource(user);
    }
}
