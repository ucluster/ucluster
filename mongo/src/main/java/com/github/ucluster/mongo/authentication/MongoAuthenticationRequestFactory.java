package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationRequestFactory;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

public class MongoAuthenticationRequestFactory implements AuthenticationRequestFactory {
    @Inject
    Injector injector;

    @Override
    public AuthenticationRequest create(Map<String, Object> request) {
        MongoAuthenticationRequest authenticationRequest = new MongoAuthenticationRequest(request);
        injector.injectMembers(authenticationRequest);
        return authenticationRequest;
    }
}
