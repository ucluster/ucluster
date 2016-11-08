package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.module.AuthenticationModule;
import com.github.ucluster.mongo.api.module.ConcernModule;
import com.github.ucluster.mongo.api.module.ConfirmModule;
import com.github.ucluster.mongo.api.module.DefinitionModule;
import com.github.ucluster.mongo.api.module.DomainModule;
import com.github.ucluster.mongo.api.module.MongoModule;
import com.github.ucluster.mongo.api.module.RequestModule;
import com.github.ucluster.mongo.api.module.SessionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jvnet.hk2.guice.bridge.api.GuiceBridge.getGuiceBridge;

public class Api extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

    @Inject
    public Api(ServiceLocator locator) throws Exception {
        LOGGER.info("bridge locator and guice injector");
        try {
            final Injector injector = Guice.createInjector(
                    Modules.override(
                            new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(ServiceLocator.class).toInstance(locator);
                                }
                            },
                            new ConcernModule(),
                            new RequestModule(),
                            new DefinitionModule(),
                            new SessionModule(),
                            new MongoModule(),
                            new ConfirmModule(),
                            new DomainModule(),
                            new AuthenticationModule()
                    ).with(overrideModules())
            );

            bridge(locator, injector);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        packages("com.github.ucluster.api");
        packages("com.github.ucluster.mongo.api");
    }

    protected List<Module> overrideModules() {
        return new ArrayList<>();
    }

    private void bridge(ServiceLocator serviceLocator, Injector injector) throws MultiException {
        getGuiceBridge().initializeGuiceBridge(serviceLocator);
        serviceLocator.getService(GuiceIntoHK2Bridge.class).bridgeGuiceInjector(injector);
    }
}
