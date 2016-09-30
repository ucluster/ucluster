package com.github.ucluster.core;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Record {

    String uuid();

    DateTime createdAt();

    void update(Property property);

    Optional<Property> property(String propertyPath);

    Collection<Property> properties();

    default void save() {
    }

    default void update() {
    }

    interface Property<T> {

        String path();

        T value();

        void value(T value);

        interface Concern {

            String type();

            Collection<Point> about();

            default boolean isAbout(Point point) {
                return about().contains(point);
            }

            void effect(Record record, String propertyPath);

            Object configuration();

            enum Point {
                VALIDATE,
                BEFORE_CREATE,
                BEFORE_UPDATE
            }
        }
    }

    interface Request {

        String type();

        Map<String, Object> metadata();

        Map<String, Object> properties();

        Map<String, Object> request();
    }
}
