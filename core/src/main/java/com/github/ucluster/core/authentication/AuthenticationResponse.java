package com.github.ucluster.core.authentication;

import com.github.ucluster.core.User;

import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;

public class AuthenticationResponse {
    private final Optional<User> user;
    private final Status status;

    private AuthenticationResponse(Optional<User> user, Status status) {
        this.user = user;
        this.status = status;
    }

    public static AuthenticationResponse success(Optional<User> user) {
        return new AuthenticationResponse(user, SUCCEEDED);
    }

    public static AuthenticationResponse fail(Optional<User> user) {
        return new AuthenticationResponse(user, FAILED);
    }

    public Status status() {
        return status;
    }

    public Optional<User> candidate() {
        return user;
    }

    public enum Status {
        SUCCEEDED, FAILED
    }
}
