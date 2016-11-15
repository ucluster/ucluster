package com.github.ucluster.core;

import com.github.ucluster.core.authentication.AuthenticationRequest;

import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> authenticate(AuthenticationRequest request);

    Optional<User> findByAccessToken(String accessToken);
}
