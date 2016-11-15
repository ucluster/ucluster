package com.github.ucluster.core.authentication;

import com.github.ucluster.core.User;

import java.util.Map;
import java.util.Optional;

public interface TokenAuthenticationService {
    String authenticate(Map<String, Object> request);

    Optional<User> verify(String token);
}
