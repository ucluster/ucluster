package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Map;

public class MongoUserDefinitionRepository implements UserDefinitionRepository {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public UserDefinition find(Map<String, Object> request) {
        final MongoDSLScript dsl = datastore.createQuery(MongoDSLScript.class)
                .field("type").equal(type(request))
                .get();

        return DSLCompiler.load(injector, dsl.script());
    }

    private String type(Map<String, Object> request) {
        return (String) request.getOrDefault("type", "default");
    }
}
