package com.github.ucluster.api;

import com.github.ucluster.core.authentication.TokenAuthenticationService;
import com.github.ucluster.core.exception.AuthenticationException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    TokenAuthenticationService authenticationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {
        try {
            String token = authenticationService.authenticate(request);
            return Response.ok(token).build();

        } catch (AuthenticationException ex) {
            return Response.status(UNAUTHORIZED).build();
        }
    }
}
