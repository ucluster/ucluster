package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.mongo.definition.RecordDefinitionRepository;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class DefinitionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<DefinitionRepository<Definition<User>>>() {
        }).to(new TypeLiteral<RecordDefinitionRepository<User>>() {
        });

        bind(new TypeLiteral<DefinitionRepository<Definition<User.Request>>>() {
        }).to(new TypeLiteral<RecordDefinitionRepository<User.Request>>() {
        });

        bind(new TypeLiteral<DefinitionRepository<Definition<User.Request.ChangeLog>>>() {
        }).to(new TypeLiteral<RecordDefinitionRepository<User.Request.ChangeLog>>() {
        });
    }
}
