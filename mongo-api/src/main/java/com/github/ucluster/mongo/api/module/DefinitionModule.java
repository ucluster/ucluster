package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.core.feature.FeatureRepository;
import com.github.ucluster.mongo.definition.MongoDefinitionRepository;
import com.github.ucluster.mongo.feature.MongoFeatureRepository;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class DefinitionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<DefinitionRepository<Definition<User>>>() {
        }).to(new TypeLiteral<MongoDefinitionRepository<User>>() {
        });

        bind(new TypeLiteral<DefinitionRepository<Definition<User.Request>>>() {
        }).to(new TypeLiteral<MongoDefinitionRepository<User.Request>>() {
        });

        bind(FeatureRepository.class).to(MongoFeatureRepository.class);
    }
}
