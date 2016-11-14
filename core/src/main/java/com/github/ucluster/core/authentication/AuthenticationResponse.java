package com.github.ucluster.core.authentication;

import com.github.ucluster.core.User;

import java.util.Optional;

public interface AuthenticationResponse {

    Status status();

    Optional<User> candidate();

    enum Status {
        SUCCEEDED, FAILED
    }
}
