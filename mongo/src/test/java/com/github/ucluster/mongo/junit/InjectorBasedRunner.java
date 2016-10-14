package com.github.ucluster.mongo.junit;

import com.github.ucluster.common.concern.CredentialConcern;
import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.TransientConcern;
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
import com.github.ucluster.mongo.request.AuthenticationRequest;
import com.github.ucluster.mongo.request.AutoApprovableRequest;
import com.github.ucluster.mongo.request.ID5AsyncRequest;
import com.github.ucluster.mongo.request.NonAutoApprovableRequest;
import com.github.ucluster.mongo.request.RecoveryRequest;
import com.github.ucluster.session.Session;
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
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;

class InjectorBasedRunner extends BlockJUnit4ClassRunner {
    protected static MongoClient mongoClient;
    protected static Injector injector;
    protected static Morphia morphia;
    protected static Datastore datastore;
    protected static JedisPool jedisPool;
    protected static Session session;

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
            morphia.getMapper().getConverters().addConverter(JodaDateTimeConverter.class);

            datastore = morphia.createDatastore(new MongoClient("127.0.0.1", 47017), "ucluster");
            datastore.ensureIndexes();
        }

        return datastore;
    }

    protected static JedisPool jedisPool() {
        if (jedisPool == null) {
            jedisPool = new JedisPool("127.0.0.1", 46379);
        }

        return jedisPool;
    }

    private List<AbstractModule> getAbstractModules() {
        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MongoClient.class).toInstance(mongoClient());
                        bind(Datastore.class).toInstance(datastore());
                        bind(Session.class).toInstance(new Session(jedisPool()));

                        bind(new TypeLiteral<Repository<? extends Record>>() {
                        }).to(MongoUserRepository.class);
                        bind(new TypeLiteral<Repository<User>>() {
                        }).to(MongoUserRepository.class);

                        bind(RequestFactory.class).to(MongoRequestFactory.class);

                        bindDefinitionRepositories();

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

                        registerRequestFactory("auto_approvable").to(new TypeLiteral<AutoApprovableRequest>() {
                        });
                        registerRequestFactory("non_auto_approvable").to(new TypeLiteral<NonAutoApprovableRequest>() {
                        });

                        registerRequestFactory("authentication").to(new TypeLiteral<AuthenticationRequest>() {
                        });
                        registerRequestFactory("recovery").to(new TypeLiteral<RecoveryRequest>() {
                        });
                        registerRequestFactory("id5_async").to(new TypeLiteral<ID5AsyncRequest>() {
                        });
                    }

                    private void bindDefinitionRepositories() {
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

                    private LinkedBindingBuilder<Record.Property.Concern> registerConcern(String type) {
                        return bind(new TypeLiteral<Record.Property.Concern>() {
                        }).annotatedWith(Names.named("property." + type + ".concern"));
                    }

                    private LinkedBindingBuilder<User.Request> registerRequestFactory(String type) {
                        return bind(new TypeLiteral<User.Request>() {
                        }).annotatedWith(Names.named("request." + type + ".factory"));
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
