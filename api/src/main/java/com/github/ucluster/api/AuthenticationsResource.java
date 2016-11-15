package com.github.ucluster.api;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    UserRepository users;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User.Token authenticate(Map<String, Object> request) {
        Optional<User> user = users.authenticate(request);
        return user.get().generateToken();
    }
}
