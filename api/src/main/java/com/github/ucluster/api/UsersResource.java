package com.github.ucluster.api;

import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("users")
public class UsersResource {
    @Inject
    Repository<User> users;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(Map<String, Object> request) {
        final User user = users.create(request);
        return Response.created(Routing.user(user)).build();
    }

    @Path("{uuid}")
    public UserResource user(@PathParam("uuid") String uuid) {
        final User user = users.uuid(uuid).orElseThrow(NotFoundException::new);
        return new UserResource(user);
    }
}
