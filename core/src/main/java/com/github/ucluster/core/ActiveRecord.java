package com.github.ucluster.core;

import org.joda.time.DateTime;

import java.util.Optional;

public interface ActiveRecord {

    String uuid();

    DateTime createdAt();

    void update(Property property);

    Optional<Property> property(String propertyPath);

    default void save() {
    }

    default void update() {
    }

    interface Property<T> {

        String path();

        T value();

        void value(T value);

        interface Specification {

            String type();

            Object configuration();
        }
    }
}
