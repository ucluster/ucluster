package com.github.ucluster.core.feature;

import java.util.Collection;
import java.util.Map;

public interface FeatureRepository {

    Collection<? extends Feature> features(Map<String, Object> metadata);
}
