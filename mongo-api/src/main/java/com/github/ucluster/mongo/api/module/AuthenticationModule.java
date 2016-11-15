package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.mongo.authentication.MongoAuthenticationServiceRegistry;
import com.google.inject.AbstractModule;

public class AuthenticationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthenticationServiceRegistry.class).to(MongoAuthenticationServiceRegistry.class);
    }
}
