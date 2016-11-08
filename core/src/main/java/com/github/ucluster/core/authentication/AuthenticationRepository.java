package com.github.ucluster.core.authentication;

import java.util.Map;

public interface AuthenticationRepository {
    Authentication authenticate(Map<String, Object> request);
}
