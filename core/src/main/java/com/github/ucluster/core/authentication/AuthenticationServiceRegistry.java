package com.github.ucluster.core.authentication;

import java.util.Map;
import java.util.Optional;

public interface AuthenticationServiceRegistry {
    Optional<AuthenticationService> find(Map<String, String> metadata);
}
