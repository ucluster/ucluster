package com.github.ucluster.api;

import com.github.ucluster.core.User;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.Page;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class RequestsResource {
    private final User user;

    public RequestsResource(User user) {
        this.user = user;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response apply(Map<String, Object> request) {
        final User.Request appliedRequest = user.apply(request);

        return Response.created(Routing.request(user, appliedRequest)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Page<? extends User.Request> requests(@QueryParam("page") int page,
                                                 @QueryParam("per-page") int perPage,
                                                 @Context ContainerRequestContext context) {
        return user.requests(Criteria.empty()).toPage(page, perPage);
    }

    @Path("{uuid}")
    public RequestResource request(@PathParam("uuid") String uuid) {
        return new RequestResource(user.request(uuid).orElseThrow(NotFoundException::new));
    }
}
