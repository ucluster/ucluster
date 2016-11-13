package com.github.ucluster.core.authentication;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;

import java.util.Optional;

public interface Authentication extends Record {
    Optional<User> user();
}
