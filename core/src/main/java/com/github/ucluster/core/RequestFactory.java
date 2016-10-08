package com.github.ucluster.core;

import java.util.Map;

public interface RequestFactory {

    User.Request create(User user, Map<String, Object> request);
}
