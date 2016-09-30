package com.github.ucluster.core.definition;

import com.github.ucluster.core.User;

import java.util.Map;

public interface UserDefinitionRepository {

    Definition<User> find(Map<String, Object> metadata);
}
