package com.github.ucluster.mongo.junit;

import com.github.ucluster.common.definition.processor.ImmutableProcessor;
import com.github.ucluster.common.definition.processor.PasswordProcessor;
import com.github.ucluster.common.definition.validator.FormatValidator;
import com.github.ucluster.common.definition.validator.RequiredValidator;
import com.github.ucluster.common.definition.validator.UniquenessValidator;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.definition.MongoUserDefinitionRepository;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
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
                        bind(UserRepository.class).to(MongoUserRepository.class);

                        bind(UserDefinitionRepository.class).to(MongoUserDefinitionRepository.class);

                        registerValidator("format", FormatValidator.class);
                        registerValidator("required", RequiredValidator.class);
                        registerValidator("uniqueness", UniquenessValidator.class);

                        registerProcessor("password", PasswordProcessor.class);
                        registerProcessor("immutable", ImmutableProcessor.class);
                    }

                    private void registerValidator(String type, Class<? extends PropertyValidator> propertyValidatorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named("property." + type + ".validator")).toInstance(propertyValidatorClass);
                    }

                    private void registerProcessor(String type, Class<? extends PropertyProcessor> propertyProcessorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named("property." + type + ".processor")).toInstance(propertyProcessorClass);
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
