package com.github.ucluster.core;

public interface RequestFactory {

    User.Request create(User user, ApiRequest request);
}
