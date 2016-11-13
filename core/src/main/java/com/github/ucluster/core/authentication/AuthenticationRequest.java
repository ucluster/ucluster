package com.github.ucluster.core.authentication;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;

import java.util.Optional;

public interface AuthenticationRequest extends Record {

    String method();

    void response(AuthenticationResponse response);

    interface AuthenticationResponse {

        Status status();

        Optional<User> candidate();

        enum Status {
            SUCCEEDED, FAILED
        }
    }
}
