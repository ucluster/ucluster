package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Map;

public class RecordDefinitionRepository<T extends Record> implements DefinitionRepository<Definition<T>> {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public Definition<T> find(Map<String, Object> metadata) {
        if (isAction(metadata)) {
            final MongoDSLScript dsl = datastore.createQuery(MongoDSLScript.class)
                    .field("model").equal("request")
                    .field("type").equal(type(metadata))
                    .get();


            if (dsl == null) {
                throw new RuntimeException("dsl not found");
            }

            return DSLCompiler.load_action(injector, dsl.script(), action(metadata));
        } else {
            final MongoDSLScript dsl = datastore.createQuery(MongoDSLScript.class)
                    .field("model").equal(model(metadata))
                    .field("type").equal(type(metadata))
                    .get();

            if (dsl == null) {
                throw new RuntimeException("dsl not found");
            }

            return DSLCompiler.load(injector, dsl.script());
        }
    }

    private String action(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("action", "approve");
    }

    private boolean isAction(Map<String, Object> metadata) {
        return "change_log".equals(model(metadata));
    }

    private String model(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("model", "user");
    }

    private String type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("type", "default");
    }
}
