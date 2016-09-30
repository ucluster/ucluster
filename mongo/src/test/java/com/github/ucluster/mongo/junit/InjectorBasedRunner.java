package com.github.ucluster.mongo.junit;

import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.PasswordConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.UniquenessConcern;
import com.github.ucluster.core.LifecycleMonitor;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.mongo.MongoLifecycleMonitor;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.definition.UserDefinitionRepository;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;

class InjectorBasedRunner extends BlockJUnit4ClassRunner {
    protected static MongoClient mongoClient;
    protected static Injector injector;
    protected static Morphia morphia;
    protected static Datastore datastore;

    InjectorBasedRunner(Class<?> klass) throws InitializationError {
        super(klass);
        try {
            injector = createInjector(getAbstractModules());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static MongoClient mongoClient() {
        if (mongoClient == null) {
            mongoClient = new MongoClient("127.0.0.1", 47017);
        }
        return mongoClient;
    }

    private static Datastore datastore() {
        if (datastore == null) {
            morphia = new Morphia();
            morphia.mapPackage("com.github.ucluster.mongo");

            datastore = morphia.createDatastore(new MongoClient("127.0.0.1", 47017), "ucluster");
            datastore.ensureIndexes();
        }

        return datastore;
    }

    private List<AbstractModule> getAbstractModules() {
        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MongoClient.class).toInstance(mongoClient());
                        bind(Datastore.class).toInstance(datastore());

                        bind(new TypeLiteral<Repository<User>>() {
                        }).to(MongoUserRepository.class);

                        bind(new TypeLiteral<DefinitionRepository<Definition<User>>>() {
                        }).to(UserDefinitionRepository.class);

                        bind(new TypeLiteral<LifecycleMonitor<User>>() {
                        }).to(new TypeLiteral<MongoLifecycleMonitor<User>>() {
                        });

                        registerConcern("format").to(new TypeLiteral<FormatConcern<User>>() {
                        });
                        registerConcern("email").to(new TypeLiteral<EmailConcern<User>>() {
                        });
                        registerConcern("required").to(new TypeLiteral<RequiredConcern<User>>() {
                        });
                        registerConcern("uniqueness").to(new TypeLiteral<UniquenessConcern<User>>() {
                        });
                        registerConcern("identity").to(new TypeLiteral<IdentityConcern<User>>() {
                        });
                        registerConcern("password").to(new TypeLiteral<PasswordConcern<User>>() {
                        });
                        registerConcern("immutable").to(new TypeLiteral<ImmutableConcern<User>>() {
                        });
                    }

                    private LinkedBindingBuilder<Record.Property.Concern<User>> registerConcern(String type) {
                        return bind(new TypeLiteral<Record.Property.Concern<User>>() {
                        }).annotatedWith(Names.named("property." + type + ".concern"));
                    }
                }}));
    }

    @Override
    protected Object createTest() throws Exception {
        Object testClass = super.createTest();
        injector.injectMembers(testClass);
        return testClass;
    }
}
