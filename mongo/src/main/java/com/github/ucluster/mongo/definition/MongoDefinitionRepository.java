package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.common.definition.DefaultRecordDefinition;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.github.ucluster.core.feature.FeatureRepository;
import com.github.ucluster.mongo.Constants;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class MongoDefinitionRepository<T extends Record> implements DefinitionRepository<Definition<T>> {
    @Inject
    protected Injector injector;

    @Inject
    protected Datastore datastore;

    @Inject
    protected FeatureRepository features;

    @Override
    public Definition<T> find(Map<String, String> metadata) {
        //TODO: refactor implementation
        if (target_model(metadata).equals(Constants.Record.USER)) {
            return load_user_definition(user_type(metadata));
        } else if (target_model(metadata).equals(Constants.Record.AUTHENTICATION)) {
            return new DefaultRecordDefinition<>(newArrayList());
        } else {
            return load_request_definition(user_type(metadata), type(metadata));
        }

    }

    private Definition<T> load_user_definition(String userType) {
        final Definition<T> definition = load_user_original_definition(userType);
        for (Definition<T> def : load_user_feature_enhanced_definition(userType)) {
            definition.merge(def);
        }

        return definition;
    }

    private Definition<T> load_user_original_definition(String userType) {
        final MongoDSLScript user_script = datastore.createQuery(MongoDSLScript.class)
                .field("userType").equal(userType)
                .field("scriptType").equal("user")
                .get();

        if (user_script == null) {
            throw new RecordTypeNotSupportedException(userType);
        }

        return DSLCompiler.load_user(injector, user_script.script());
    }

    private List<Definition<T>> load_user_feature_enhanced_definition(String userType) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_type", userType);

        return features.features(metadata)
                .stream()
                .map(feature -> feature.definition(User.class))
                .filter(Optional::isPresent)
                .map(op -> (Definition<T>) op.get())
                .collect(Collectors.toList());
    }

    private Definition<T> load_request_definition(String userType, String type) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_type", userType);
        metadata.put("type", type);

        final Optional<Definition<User.Request>> definition = features.features(metadata)
                .stream()
                .map(feature -> feature.definition(User.Request.class, type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        return (Definition<T>) definition.orElseThrow(() -> new RecordTypeNotSupportedException(type));
    }

    private String target_model(Map<String, String> metadata) {
        return metadata.getOrDefault("model", Constants.Record.USER);
    }

    private String user_type(Map<String, String> metadata) {
        return metadata.getOrDefault("user_type", "default");
    }

    private String type(Map<String, String> metadata) {
        return metadata.getOrDefault("type", "default");
    }
}
