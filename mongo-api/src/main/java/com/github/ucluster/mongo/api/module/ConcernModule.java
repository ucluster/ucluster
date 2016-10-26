package com.github.ucluster.mongo.api.module;

import com.github.ucluster.common.concern.CredentialConcern;
import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.TransientConcern;
import com.github.ucluster.common.concern.UniquenessConcern;
import com.github.ucluster.core.Record;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

public class ConcernModule extends AbstractModule {
    @Override
    protected void configure() {
        registerConcern("format").to(new TypeLiteral<FormatConcern>() {
        });
        registerConcern("email").to(new TypeLiteral<EmailConcern>() {
        });
        registerConcern("required").to(new TypeLiteral<RequiredConcern>() {
        });
        registerConcern("uniqueness").to(new TypeLiteral<UniquenessConcern>() {
        });
        registerConcern("identity").to(new TypeLiteral<IdentityConcern>() {
        });
        registerConcern("credential").to(new TypeLiteral<CredentialConcern>() {
        });
        registerConcern("immutable").to(new TypeLiteral<ImmutableConcern>() {
        });
        registerConcern("transient").to(new TypeLiteral<TransientConcern>() {
        });
    }

    private LinkedBindingBuilder<Record.Property.Concern> registerConcern(String type) {
        return bind(new TypeLiteral<Record.Property.Concern>() {
        }).annotatedWith(Names.named("property." + type + ".concern"));
    }
}
