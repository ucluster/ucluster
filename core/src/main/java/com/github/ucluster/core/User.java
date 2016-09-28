package com.github.ucluster.core;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Optional;

public interface User {

    String uuid();

    DateTime createdAt();

    void authenticate(Property identityProperty, String password);

    void update(Property property);

    Optional<Property> property(String key);

    interface Property<T> {

        String key();

        T value();
    }

    interface Request {

        String type();

        Map<String, Object> metadata();

        Map<String, Object> properties();

        Map<String, Object> request();
    }
}
