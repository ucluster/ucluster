package com.github.ucluster.core;

public interface UserRepository extends Repository<User> {

    User create(User.Request request);
}