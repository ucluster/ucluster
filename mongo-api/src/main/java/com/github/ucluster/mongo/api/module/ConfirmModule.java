package com.github.ucluster.mongo.api.module;

import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.mongo.confirmation.MongoConfirmationRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class ConfirmModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<ConfirmationRegistry>() {
        }).to(MongoConfirmationRegistry.class);
    }
}
