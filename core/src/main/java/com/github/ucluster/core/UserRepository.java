package com.github.ucluster.core;

import java.util.Map;
import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> authenticate(Map<String, Object> request);
}
