package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationLog;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.exception.AuthenticationException;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class MongoAuthenticationService implements AuthenticationService {
    @Inject
    AuthenticationServiceRegistry registry;

    @Inject
    Injector injector;

    @Override
    public AuthenticationResponse authenticate(Map<String, Object> request) {
        Optional<AuthenticationService> service = registry.find(methodOf(request));

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(request);

        audit(request, response);

        return response;
    }

    private void audit(Map<String, Object> request, AuthenticationResponse response) {
        AuthenticationLog authenticationLog = new MongoAuthenticationLog(request, response);
        injector.injectMembers(authenticationLog);
        authenticationLog.save();
    }

    private String methodOf(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return (String) metadata.getOrDefault("method", "password");
    }
}
