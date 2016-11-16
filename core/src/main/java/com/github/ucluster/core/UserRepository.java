package com.github.ucluster.core;

import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> authenticate(ApiRequest request);

    Optional<User> findByAccessToken(String accessToken);
}
