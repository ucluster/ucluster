package com.github.ucluster.core;

import org.joda.time.DateTime;

import java.util.Optional;

public interface User {

    String uuid();

    DateTime createdAt();

    void authenticate(Property identityProperty, String password);

    void update(Property property);

    Optional<Property> property(String key);

    interface Property {

        String key();

        String value();
    }
}
