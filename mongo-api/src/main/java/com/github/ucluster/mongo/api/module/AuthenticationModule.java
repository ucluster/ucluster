package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.feature.password.authentication.PasswordAuthenticationService;
import com.github.ucluster.mongo.authentication.MongoAuthenticationRepository;
import com.github.ucluster.mongo.authentication.MongoAuthenticationServiceRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class AuthenticationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthenticationServiceRegistry.class).to(MongoAuthenticationServiceRegistry.class);
        bind(new TypeLiteral<AuthenticationService>(){}).annotatedWith(
                Names.named("authentication.password.method")).to(new TypeLiteral<PasswordAuthenticationService>(){});
        bind(AuthenticationRepository.class).to(MongoAuthenticationRepository.class);
    }
}
