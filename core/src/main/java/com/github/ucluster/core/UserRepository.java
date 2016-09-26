package com.github.ucluster.core;

import java.util.Map;

public interface UserRepository {

    User create(Map<String, Object> request);

    User uuid(String uuid);
}