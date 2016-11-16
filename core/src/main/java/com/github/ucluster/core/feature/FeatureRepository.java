package com.github.ucluster.core.feature;

import com.github.ucluster.core.ApiRequest;

import java.util.Collection;

public interface FeatureRepository {

    Collection<? extends Feature> features(ApiRequest.Metadata metadata);
}
