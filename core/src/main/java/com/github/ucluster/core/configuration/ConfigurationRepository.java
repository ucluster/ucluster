package com.github.ucluster.core.configuration;

import java.util.Map;

public interface ConfigurationRepository {

    Map<String, Object> find(Map<String, Object> metadata);
}
