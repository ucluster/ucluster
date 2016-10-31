package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoRequestFactory;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

public class RequestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RequestFactory.class).to(MongoRequestFactory.class);
    }

    protected LinkedBindingBuilder<User.Request> registerRequest(String type) {
        return bind(new TypeLiteral<User.Request>() {
        }).annotatedWith(Names.named("request." + type + ".class"));
    }
}
