package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.github.ucluster.mongo.Constants;
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
        return load(dsl(metadata));
    }

    private MongoDSLScript dsl(Map<String, Object> metadata) {
        final MongoDSLScript dsl = datastore.createQuery(MongoDSLScript.class)
                .field("model").equal(target_model(metadata))
                .field("type").equal(type(metadata))
                .get();

        if (dsl == null) {
            throw new RecordTypeNotSupportedException(type(metadata));
        }
        return dsl;
    }

    private String target_model(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("model", Constants.Record.USER);
    }

    private String type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("type", "default");
    }

    private Definition<T> load(MongoDSLScript dsl) {
        return DSLCompiler.load(injector, dsl.script());
    }
}
