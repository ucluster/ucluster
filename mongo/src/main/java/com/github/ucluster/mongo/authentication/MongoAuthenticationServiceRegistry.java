package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.configuration.ConfigurationRepository;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class MongoAuthenticationServiceRegistry implements AuthenticationServiceRegistry {

    @Inject
    Injector injector;

    @Inject
    ConfigurationRepository configurations;

    MongoAuthenticationServiceRegistry() {
    }

    @Override
    public Optional<AuthenticationService> find(ApiRequest.Metadata metadata) {
        try {
            final AuthenticationService service = createAuthenticationService(metadata.type());
            injector.injectMembers(service);

            return Optional.of(service);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private AuthenticationService createAuthenticationService(String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        final Class<? extends AuthenticationService> serviceClass = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService>() {
        }, Names.named("authentication." + type + ".method"))).getClass();

        final Constructor<? extends AuthenticationService> constructor = serviceClass.getConstructor();
        final AuthenticationService service = constructor.newInstance();

        return injectConfiguration(service, getConfiguration(type));
    }

    private AuthenticationService injectConfiguration(AuthenticationService service, Map<String, Object> configuration) throws NoSuchMethodException, IllegalAccessException {
        final Class<? extends AuthenticationService> klass = service.getClass();

        for (Field field : klass.getDeclaredFields()) {
            if (configuration.containsKey(field.getName())) {
                field.setAccessible(true);
                field.set(service, configuration.get(field.getName()));
            }
        }

        return service;
    }

    private Map<String, Object> getConfiguration(String type) {
        return configurations.find(ImmutableMap.<String, Object>builder()
                .put("type", type)
                .build()
        );
    }
}
