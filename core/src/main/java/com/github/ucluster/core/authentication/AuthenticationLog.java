package com.github.ucluster.core.authentication;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.authentication.AuthenticationResponse.Status;

import java.util.Optional;

public interface AuthenticationLog extends Record {
    Optional<User> candidate();
    Status status();
}
