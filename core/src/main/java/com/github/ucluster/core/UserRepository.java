package com.github.ucluster.core;

import java.util.Map;

public interface UserRepository extends Repository<User> {

    User authenticate(Map<String, Object> request);
}
