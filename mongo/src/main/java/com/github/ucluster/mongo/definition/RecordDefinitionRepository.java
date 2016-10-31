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
import java.util.List;
import java.util.Map;

public class RecordDefinitionRepository<T extends Record> implements DefinitionRepository<Definition<T>> {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public Definition<T> find(Map<String, Object> metadata) {
        if (target_model(metadata).equals(Constants.Record.USER)) {
            return load_user_definition(user_type(metadata));
        } else {
            return load_request_definition(user_type(metadata), type(metadata));
        }
    }

    private Definition<T> load_user_definition(String userType) {
        final MongoDSLScript script = datastore.createQuery(MongoDSLScript.class)
                .field("userType").equal(userType)
                .field("scriptType").equal("user")
                .get();

        if (script == null) {
            throw new RecordTypeNotSupportedException(userType);
        }

        return DSLCompiler.load_user(injector, script.script());
    }

    private Definition<T> load_request_definition(String userType, String type) {
        final List<MongoDSLScript> scripts = datastore.createQuery(MongoDSLScript.class)
                .field("userType").equal(userType)
                .field("scriptType").equal("feature")
                .asList();

        for (MongoDSLScript script : scripts) {
            final Definition<T> definition = DSLCompiler.load_request(injector, script.script(), type);
            if (definition != null) {
                return definition;
            }
        }

        throw new RecordTypeNotSupportedException(type);
    }

    private String target_model(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("model", Constants.Record.USER);
    }

    private String user_type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("user_type", "default");
    }

    private String type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("type", "default");
    }
}
