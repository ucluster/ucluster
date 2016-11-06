package com.github.ucluster.mongo.api.junit;

import com.github.ucluster.mongo.api.Api;
import com.github.ucluster.mongo.api.Env;
import com.github.ucluster.mongo.api.module.RequestModule;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.github.ucluster.session.Session;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.jvnet.hk2.guice.bridge.api.GuiceBridge.getGuiceBridge;

public class InjectorBasedRunner extends BlockJUnit4ClassRunner {
    private static final String SERVER_URI = "http://localhost:8888";

    protected static MongoClient mongoClient;
    protected static Datastore datastore;
    protected static JedisPool jedisPool;

    protected static ServiceLocator locator = Injections.createLocator();

    public InjectorBasedRunner(Class<?> klass) throws InitializationError {
        super(klass);

        List<AbstractModule> modules = getAbstractModules();
        try {
            Injector injector = createInjector(Modules.override(modules).with(new TestModule()));
            bridge(locator, injector);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        locator.inject(this);
    }

    private static void bridge(ServiceLocator serviceLocator, Injector injector) {
        getGuiceBridge().initializeGuiceBridge(serviceLocator);
        serviceLocator.getService(GuiceIntoHK2Bridge.class).bridgeGuiceInjector(injector);
    }

    private List<AbstractModule> getAbstractModules() {
        List<AbstractModule> modules = new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bindConstant().annotatedWith(Names.named("server_uri")).to(SERVER_URI);
                        bind(ServiceLocator.class).toInstance(locator);

                        bind(Session.class).toInstance(new Session(jedisPool()));

                        bind(ApiSupport.ClientConfigurator.class).toInstance(config -> {
                            config.register(JacksonFeature.class);
                            config.connectorProvider(new ApacheConnectorProvider());
                        });

                        bind(ApiSupport.SetUp.class).toInstance(() -> {
                        });
                    }
                }}));
        modules.addAll(getModules());
        return modules;
    }

    protected Pool<Jedis> jedisPool() {
        if (jedisPool == null) {
            jedisPool = new JedisPool(Env.getRedisHost(), Env.getRedisPort());
        }

        return jedisPool;
    }

    protected Datastore datastore() {
        if (datastore == null) {
            Morphia morphia = new Morphia();
            morphia.mapPackage("com.github.ucluster.mongo");
            morphia.getMapper().getConverters().addConverter(JodaDateTimeConverter.class);

            datastore = morphia.createDatastore(mongoClient(), "ucluster");
            datastore.ensureIndexes();
        }

        return datastore;
    }

    protected MongoClient mongoClient() {
        if (mongoClient == null) {
            mongoClient = new MongoClient(Env.getMongoHost(), Env.getMongoPort());
        }

        return mongoClient;
    }

    protected List<AbstractModule> getModules() {
        return Collections.emptyList();
    }

    @Override
    protected Object createTest() throws Exception {
        Object testClass = super.createTest();
        locator.inject(testClass);
        return testClass;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    private static class TestRequestModule extends RequestModule {
        @Override
        protected void configure() {
            super.configure();
        }
    }

    public static class ApiTestResourceConfig extends Api {
        @Inject
        public ApiTestResourceConfig(ServiceLocator locator) throws Exception {
            super(locator);
        }

        @Override
        protected List<Module> overrideModules() {
            return asList(new TestRequestModule());
        }
    }
}
