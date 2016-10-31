package com.github.ucluster.api;

import com.github.ucluster.core.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class RequestResource {
    private final User.Request request;

    public RequestResource(User.Request request) {
        this.request = request;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User.Request request(@PathParam("uuid") String uuid) {
        return request;
    }

    @PUT
    @Path("approved")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response approve(Map<String, Object> detail) {
        request.approve(detail);
        return Response.ok().build();
    }
}
