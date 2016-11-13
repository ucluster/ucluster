package com.github.ucluster.api;

import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationRequestFactory;

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
    AuthenticationRequestFactory authenticationRequestFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {
        AuthenticationRequest authenticationRequest = authenticationRequestFactory.create(request);
        AuthenticationResponse response = authenticationRequest.execute();

        if (response.status() == SUCCEEDED) {
            return Response.ok().build();
        }

        return Response.status(401).build();
    }
}
