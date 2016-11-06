package com.github.ucluster.mongo.feature;

import com.github.ucluster.core.feature.Feature;
import com.github.ucluster.core.feature.FeatureRepository;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MongoFeatureRepository implements FeatureRepository {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public Collection<? extends Feature> features(Map<String, Object> metadata) {
        final List<MongoFeature> features = datastore.createQuery(MongoFeature.class)
                .field("userType").equal(userType(metadata))
                .field("scriptType").equal("feature")
                .asList();

        features.forEach(feature -> injector.injectMembers(feature));

        return features;
    }

    private String userType(Map<String, Object> metadata) {
        return (String) metadata.getOrDefault("user_type", "default");
    }
}
