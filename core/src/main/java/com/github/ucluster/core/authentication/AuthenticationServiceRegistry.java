package com.github.ucluster.core.authentication;

import com.github.ucluster.core.AuthenticationService;

import java.util.Optional;

public interface AuthenticationServiceRegistry {
    Optional<AuthenticationService> find(String type);
}
