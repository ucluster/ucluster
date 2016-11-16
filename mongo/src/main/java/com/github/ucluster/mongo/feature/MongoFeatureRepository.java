package com.github.ucluster.mongo.feature;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.feature.Feature;
import com.github.ucluster.core.feature.FeatureRepository;
import com.google.inject.Injector;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

public class MongoFeatureRepository implements FeatureRepository {
    @Inject
    Injector injector;

    @Inject
    protected Datastore datastore;

    @Override
    public Collection<? extends Feature> features(ApiRequest.Metadata metadata) {
        final List<MongoFeature> features = datastore.createQuery(MongoFeature.class)
                .field("userType").equal(metadata.userType())
                .field("scriptType").equal("feature")
                .asList();

        features.forEach(feature -> injector.injectMembers(feature));

        return features;
    }
}
