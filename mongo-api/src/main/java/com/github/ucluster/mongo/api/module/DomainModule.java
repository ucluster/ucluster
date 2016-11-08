package com.github.ucluster.mongo.api.module;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.configuration.ConfigurationRepository;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.configuration.MongoConfigurationRepository;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class DomainModule extends AbstractModule {
    @Override
    protected void configure() {
        //for uniqueness concern
        bind(new TypeLiteral<Repository<? extends Record>>() {
        }).to(MongoUserRepository.class);

        bind(new TypeLiteral<Repository<User>>() {
        }).to(MongoUserRepository.class);

        bind(ConfigurationRepository.class).to(MongoConfigurationRepository.class);
    }
}
