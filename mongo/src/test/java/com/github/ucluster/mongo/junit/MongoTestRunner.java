package com.github.ucluster.mongo.junit;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class MongoTestRunner extends InjectorBasedRunner {
    public MongoTestRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    private final TestRule clearMongo = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                final MongoDatabase cacheServer = mongoClient.getDatabase("ucluster");
                if (cacheServer != null) {
                    final MongoCollection<Document> cars = cacheServer.getCollection("users");
                    if (cars != null) {
                        cars.deleteMany(new Document());
                    }
                }
            }
        }
    };

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = new ArrayList<>();
        rules.add(clearMongo);
        rules.addAll(super.getTestRules(target));
        return rules;
    }
}