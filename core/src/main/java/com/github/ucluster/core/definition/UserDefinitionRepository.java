package com.github.ucluster.core.definition;

import java.util.Map;

public interface UserDefinitionRepository {

    UserDefinition find(Map<String, Object> request);
}
