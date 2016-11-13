package com.github.ucluster.api;

import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse.Status.SUCCEEDED;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    AuthenticationRepository authenticationRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {
        AuthenticationResponse response = authenticationRepository.authenticate(request);

        if (response.status() == SUCCEEDED) {
            return Response.ok().build();
        }

        return Response.status(401).build();
    }
}
