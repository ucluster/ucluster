package com.github.ucluster.api;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("authentications")
public class AuthenticationsResource {

    @Inject
    UserRepository users;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Map<String, Object> request) {

        Optional<User> user = users.authenticate(request);

        if (!user.isPresent()) {
            return Response.status(UNAUTHORIZED).build();
        }

        return Response.ok(issueToken(user)).build();
    }

    private String issueToken(Optional<User> user) {
        return Jwts.builder()
                .setSubject((String) user.get().property("username").get().value())
                .signWith(SignatureAlgorithm.HS512, MacProvider.generateKey())
                .compact();
    }
}
