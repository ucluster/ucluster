package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.configuration.ConfigurationRepository;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Optional;

public class MongoAuthenticationServiceRegistry implements AuthenticationServiceRegistry {

    @Inject
    Injector injector;


    @Inject
    protected ConfigurationRepository configurations;

    MongoAuthenticationServiceRegistry() {

    }

    @Override
    public Optional<AuthenticationService> find(String type) {
        try {
            final Class<? extends AuthenticationService> serviceClass = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService>() {
            }, Names.named("authentication." + type + ".method"))).getClass();

            final Constructor<? extends AuthenticationService> constructor = serviceClass.getConstructor(Object.class);
            AuthenticationService service = constructor.newInstance(configurations.find(ImmutableMap.<String, Object>builder()
                    .put("type", type)
                    .build()
            ));
            injector.injectMembers(service);
            return Optional.of(service);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
