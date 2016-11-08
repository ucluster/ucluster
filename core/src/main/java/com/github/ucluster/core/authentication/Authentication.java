package com.github.ucluster.core.authentication;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;

import java.util.Optional;

public interface Authentication extends Record {

    String type();

    Optional<User> user();

    Status status();

    enum Status {
        SUCCESS, FAIL
    }
}
