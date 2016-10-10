package com.github.ucluster.mongo.api;

import com.github.ucluster.common.concern.CredentialConcern;
import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.UniquenessConcern;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.mongo.MongoRequestFactory;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.github.ucluster.mongo.definition.RecordDefinitionRepository;
import com.github.ucluster.mongo.request.MongoAuthenticationRequest;
import com.github.ucluster.mongo.request.MongoRecoveryRequest;
import com.github.ucluster.session.Session;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jvnet.hk2.guice.bridge.api.GuiceBridge.getGuiceBridge;

/**
 * Api
 * <p>
 * Configure guice injection. Need to be split later
 */
public class Api extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

    private JedisPool jedisPool;
    private Datastore datastore;
    private Morphia morphia;

    @Inject
    public Api(ServiceLocator locator) throws Exception {
        LOGGER.info("bridge locator and guice injector");
        try {
            final Injector injector = Guice.createInjector(
                    Modules.override(
                            domainModule(locator)
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

    protected Module domainModule(ServiceLocator locator) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(ServiceLocator.class).toInstance(locator);

                bind(Datastore.class).toInstance(datastore());
                bind(Session.class).toInstance(new Session(jedisPool()));
                bind(new TypeLiteral<Repository<? extends Record>>() {
                }).to(MongoUserRepository.class);
                bind(new TypeLiteral<Repository<User>>() {
                }).to(MongoUserRepository.class);

                bind(RequestFactory.class).to(MongoRequestFactory.class);

                bind(new TypeLiteral<DefinitionRepository<Definition<User>>>() {
                }).to(new TypeLiteral<RecordDefinitionRepository<User>>() {
                });

                bind(new TypeLiteral<DefinitionRepository<Definition<User.Request>>>() {
                }).to(new TypeLiteral<RecordDefinitionRepository<User.Request>>() {
                });

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

                registerRequestFactory("authentication").to(new TypeLiteral<MongoAuthenticationRequest>() {
                });
                registerRequestFactory("recovery").to(new TypeLiteral<MongoRecoveryRequest>() {
                });
            }

            private LinkedBindingBuilder<Record.Property.Concern> registerConcern(String type) {
                return bind(new TypeLiteral<Record.Property.Concern>() {
                }).annotatedWith(Names.named("property." + type + ".concern"));
            }

            private LinkedBindingBuilder<User.Request> registerRequestFactory(String type) {
                return bind(new TypeLiteral<User.Request>() {
                }).annotatedWith(Names.named("request." + type + ".factory"));
            }
        };
    }

    private Pool<Jedis> jedisPool() {
        if (jedisPool == null) {
            if (Env.getRedisPassword() != null) {
                jedisPool = new JedisPool(new GenericObjectPoolConfig(),
                        Env.getRedisHost(),
                        Env.getRedisPort(),
                        Protocol.DEFAULT_TIMEOUT,
                        Env.getRedisPassword());
            } else {
                jedisPool = new JedisPool(Env.getRedisHost(), Env.getRedisPort());
            }
        }
        return jedisPool;
    }

    private Datastore datastore() {
        if (datastore == null) {
            morphia = new Morphia();
            morphia.mapPackage("com.github.ucluster.mongo");
            morphia.getMapper().getConverters().addConverter(JodaDateTimeConverter.class);

            datastore = morphia.createDatastore(new MongoClient("127.0.0.1", 47017), "ucluster");
            datastore.ensureIndexes();
        }

        return datastore;
    }

    protected List<Module> overrideModules() {
        return new ArrayList<>();
    }

    private void bridge(ServiceLocator serviceLocator, Injector injector) throws MultiException {
        getGuiceBridge().initializeGuiceBridge(serviceLocator);
        serviceLocator.getService(GuiceIntoHK2Bridge.class).bridgeGuiceInjector(injector);
    }
}
