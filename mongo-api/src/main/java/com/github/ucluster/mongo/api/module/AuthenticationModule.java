package com.github.ucluster.mongo.api.module;

import com.github.ucluster.common.authentication.JWTAuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationLog;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.authentication.TokenAuthenticationService;
import com.github.ucluster.feature.password.authentication.PasswordAuthenticationService;
import com.github.ucluster.mongo.authentication.MongoAuthenticationLog;
import com.github.ucluster.mongo.authentication.MongoAuthenticationServiceRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

public class AuthenticationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthenticationServiceRegistry.class).to(MongoAuthenticationServiceRegistry.class);
        bind(TokenAuthenticationService.class).to(JWTAuthenticationService.class);
        bind(AuthenticationLog.class).to(MongoAuthenticationLog.class);
        registerAuthenticationService("password").to(new TypeLiteral<PasswordAuthenticationService>() {
        });
    }

    private LinkedBindingBuilder<AuthenticationService> registerAuthenticationService(String type) {
        return bind(new TypeLiteral<AuthenticationService>() {
        }).annotatedWith(Names.named("authentication." + type + ".method"));
    }
}
