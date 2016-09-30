package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Map;

public class UserDefinitionRepository implements DefinitionRepository<Definition<User>> {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public Definition<User> find(Map<String, Object> metadata) {
        final MongoDSLScript dsl = datastore.createQuery(MongoDSLScript.class)
                .field("type").equal(type(metadata))
                .get();

        return DSLCompiler.load(injector, dsl.script());
    }

    private String type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("user_type", "default");
    }
}
