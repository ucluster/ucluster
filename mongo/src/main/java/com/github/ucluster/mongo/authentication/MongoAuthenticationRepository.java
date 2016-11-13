package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationRequestFactory;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.exception.AuthenticationException;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class MongoAuthenticationRepository implements AuthenticationRepository {

    @Inject
    Injector injector;

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

    private MongoAuthenticationRequest mongoAuthentication() {
//        MongoAuthenticationRequest authentication = new MongoAuthenticationRequest();
//        injector.injectMembers(authentication);
//        return authentication;
        return null;
    }

    private String methodOf(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return (String) metadata.getOrDefault("method", "password");
    }
}
