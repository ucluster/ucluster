package com.github.ucluster.mongo.definition;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.common.definition.DefaultRecordDefinition;
import com.github.ucluster.core.ApiRequest;
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
import java.util.List;
import java.util.Objects;
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
    public Definition<T> find(ApiRequest.Metadata metadata) {
        if (Objects.equals(metadata.model(), Constants.Record.USER)) {
            return load_user_definition(metadata);
        } else if (Objects.equals(metadata.model(), Constants.Record.AUTHENTICATION)) {
            //TODO: authentication definition
            return new DefaultRecordDefinition<>(newArrayList());
        } else {
            return load_request_definition(metadata);
        }
    }

    private Definition<T> load_user_definition(ApiRequest.Metadata metadata) {
        final Definition<T> definition = load_user_original_definition(metadata);
        for (Definition<T> def : load_user_feature_enhanced_definition(metadata)) {
            definition.merge(def);
        }

        return definition;
    }

    private Definition<T> load_user_original_definition(ApiRequest.Metadata metadata) {
        final MongoDSLScript user_script = datastore.createQuery(MongoDSLScript.class)
                .field("userType").equal(metadata.userType())
                .field("scriptType").equal("user")
                .get();

        if (user_script == null) {
            throw new RecordTypeNotSupportedException(metadata.userType());
        }

        return DSLCompiler.load_user(injector, user_script.script());
    }

    private List<Definition<T>> load_user_feature_enhanced_definition(ApiRequest.Metadata metadata) {
        return features.features(metadata)
                .stream()
                .map(feature -> feature.definition(User.class))
                .filter(Optional::isPresent)
                .map(op -> (Definition<T>) op.get())
                .collect(Collectors.toList());
    }

    private Definition<T> load_request_definition(ApiRequest.Metadata metadata) {
        final Optional<Definition<User.Request>> definition = features.features(metadata)
                .stream()
                .map(feature -> feature.definition(User.Request.class, metadata.type()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        return (Definition<T>) definition.orElseThrow(() -> new RecordTypeNotSupportedException(metadata.type()));
    }
}
