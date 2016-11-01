package com.github.ucluster.mongo.configuration;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.configuration.ConfigurationRepository;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class MongoConfigurationRepository implements ConfigurationRepository {
    @Inject
    protected Datastore datastore;

    @Override
    public Map<String, Object> find(Map<String, Object> metadata) {
        //TODO: current we only have config type for authentication service
        final List<MongoDSLScript> scripts = datastore.createQuery(MongoDSLScript.class)
                .field("userType").equal(user_type(metadata))
                .field("scriptType").equal("feature")
                .asList();

        for (MongoDSLScript script : scripts) {
            final Map<String, Object> definition = DSLCompiler.load_auth_config(script.script(), type(metadata));
            if (definition != null) {
                return definition;
            }
        }

        throw new RuntimeException("unable to find configuration");
    }

    private String user_type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("user_type", "default");
    }

    private String type(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("type", "default");
    }
}
