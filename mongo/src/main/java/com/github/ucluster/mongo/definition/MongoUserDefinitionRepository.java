package com.github.ucluster.mongo.definition;

import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.mongo.dsl.DSL;
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
        final DSL dsl = datastore.createQuery(DSL.class)
                .get();

        injector.injectMembers(dsl);

        return dsl.userDefinition();
    }
}
