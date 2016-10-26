package com.github.ucluster.mongo.api.module;

import com.github.ucluster.mongo.api.Env;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.google.inject.AbstractModule;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class MongoModule extends AbstractModule {
    private Datastore datastore;

    @Override
    protected void configure() {
        bind(Datastore.class).toInstance(datastore());
    }

    private Datastore datastore() {
        if (datastore == null) {
            Morphia morphia = new Morphia();
            morphia.mapPackage("com.github.ucluster.mongo");
            morphia.getMapper().getConverters().addConverter(JodaDateTimeConverter.class);

            datastore = morphia.createDatastore(new MongoClient(Env.getMongoHost(), Env.getMongoPort()), "ucluster");
            datastore.ensureIndexes();
        }

        return datastore;
    }
}
