package com.github.ucluster.api;

import com.github.ucluster.core.authentication.Authentication;
import com.github.ucluster.core.authentication.AuthenticationRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.ucluster.core.authentication.Authentication.Status.SUCCESS;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    AuthenticationRepository authenticationRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {
        Authentication authentication = authenticationRepository.authenticate(request);
        if (authentication.status() == SUCCESS) {
            return Response.ok().build();
        }
        return Response.status(401).build();
    }
}
