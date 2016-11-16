package com.github.ucluster.core.authentication;

import com.github.ucluster.core.ApiRequest;

import java.util.Optional;

public interface AuthenticationServiceRegistry {
    Optional<AuthenticationService> find(ApiRequest.Metadata metadata);
}
