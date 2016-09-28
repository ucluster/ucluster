package com.github.ucluster.core;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Optional;

public interface User {

    String uuid();

    DateTime createdAt();

    void authenticate(Property identityProperty, Property passwordProperty);

    void update(Property property);

    Optional<Property> property(String propertyPath);

    interface Property<T> {

        String path();

        T value();

        void value(T value);

        interface Specification {

            String type();

            Object configuration();
        }
    }

    interface Request {

        String type();

        Map<String, Object> metadata();

        Map<String, Object> properties();

        Map<String, Object> request();
    }
}
