package com.github.ucluster.api;

import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    @Named("authentication.mongo.method")
    AuthenticationService authenticationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {

        AuthenticationResponse response = authenticationService.authenticate(request);

        if (response.status() == FAILED) {
            return Response.status(UNAUTHORIZED).build();
        }

        return Response.ok(issueToken(response)).build();
    }

    private String issueToken(AuthenticationResponse response) {
        return Jwts.builder()
                .setSubject((String) response.candidate().get().property("username").get().value())
                .signWith(SignatureAlgorithm.HS512, MacProvider.generateKey())
                .compact();
    }
}
