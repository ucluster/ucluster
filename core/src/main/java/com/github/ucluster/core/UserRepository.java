package com.github.ucluster.core;

import java.util.Map;
import java.util.Optional;

public interface UserRepository {

    User create(Map<String, Object> request);

    User uuid(String uuid);

    Optional<User> find(User.Property property);
}