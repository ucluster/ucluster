package com.github.ucluster.core.authentication;

import java.util.Optional;

public interface AuthenticationServiceRegistry {
    Optional<AuthenticationService> find(String type);
}
