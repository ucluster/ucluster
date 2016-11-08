package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.Authentication;
import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.AuthenticationException;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

public class MongoAuthenticationRepository implements AuthenticationRepository {

    @Inject
    Injector injector;

    @Inject
    AuthenticationServiceRegistry registry;

    MongoAuthenticationRepository() {

    }

    @Override
    public Authentication authenticate(Map<String, Object> request) {
        AuthenticationService authenticationService = registry.find(methodOf(request)).get();

        MongoAuthentication authentication = mongoAuthentication();

        try {
            User user = authenticationService.authenticate(request);
            return authentication.success(user, request).build();
        } catch (AuthenticationException ex) {
            return authentication.fail(request).build();
        }
    }

    private MongoAuthentication mongoAuthentication() {
        MongoAuthentication authentication = new MongoAuthentication();
        injector.injectMembers(authentication);
        return authentication;
    }

    private String methodOf(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return (String) metadata.get("method");
    }
}
