package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoRequestFactory;
import com.github.ucluster.mongo.request.AuthenticationRequest;
import com.github.ucluster.mongo.request.RecoveryRequest;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

public class RequestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RequestFactory.class).to(MongoRequestFactory.class);

        registerRequestFactory("authentication").to(new TypeLiteral<AuthenticationRequest>() {
        });
        registerRequestFactory("recovery").to(new TypeLiteral<RecoveryRequest>() {
        });
    }

    private LinkedBindingBuilder<User.Request> registerRequestFactory(String type) {
        return bind(new TypeLiteral<User.Request>() {
        }).annotatedWith(Names.named("request." + type + ".factory"));
    }
}
