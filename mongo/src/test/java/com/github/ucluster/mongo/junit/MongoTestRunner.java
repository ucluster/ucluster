package com.github.ucluster.mongo.junit;

import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

import static com.github.ucluster.mongo.junit.ResourceReader.read;

public class MongoTestRunner extends InjectorBasedRunner {
    public MongoTestRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    private final TestRule loadDSL = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            try {
                datastore.save(new MongoDSLScript("user", "default", read("user_dsl.js")));
                datastore.save(new MongoDSLScript("request", "default", read("request_dsl.js")));
                base.evaluate();
            } finally {
                final MongoDatabase database = mongoClient.getDatabase("ucluster");
                if (database != null) {
                    final MongoCollection<Document> dsl = database.getCollection("dsl");
                    if (dsl != null) {
                        dsl.deleteMany(new Document());
                    }
                }
            }
        }
    };

    private final TestRule clearMongo = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                final MongoDatabase database = mongoClient.getDatabase("ucluster");
                if (database != null) {
                    final MongoCollection<Document> users = database.getCollection("users");
                    if (users != null) {
                        users.deleteMany(new Document());
                    }

                    final MongoCollection<Document> requests = database.getCollection("user_requests");
                    if (requests != null) {
                        requests.deleteMany(new Document());
                    }
                }
            }
        }
    };

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = new ArrayList<>();
        rules.add(loadDSL);
        rules.add(clearMongo);
        rules.addAll(super.getTestRules(target));
        return rules;
    }
}