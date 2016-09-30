package com.github.ucluster.core;

import java.util.Optional;

public interface UserRepository {

    User create(User.Request request);

    Optional<User> uuid(String uuid);

    Optional<User> find(ActiveRecord.Property property);
}