package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationRequestFactory;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.exception.AuthenticationException;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class MongoAuthenticationRepository implements AuthenticationRepository {

    @Inject
    AuthenticationServiceRegistry registry;

    @Inject
    AuthenticationRequestFactory authenticationRequestFactory;

    MongoAuthenticationRepository() {

    }

    @Override
    public AuthenticationResponse authenticate(Map<String, Object> request) {
        AuthenticationRequest authenticationRequest = authenticationRequestFactory.create(request);
        Optional<AuthenticationService> service = registry.find(authenticationRequest.method());

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(authenticationRequest);

        audit(authenticationRequest, response);

        return response;
    }

    private void audit(AuthenticationRequest request, AuthenticationResponse response) {
        request.response(response);
        request.save();
    }
}
