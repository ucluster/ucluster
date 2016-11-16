package com.github.ucluster.core;

import com.github.ucluster.core.definition.Definition;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Optional;

public interface Record {

    String uuid();

    DateTime createdAt();

    void property(Property property);

    <V> void property(String path, V value);

    Optional<Property> property(String path);

    Collection<Property> properties();

    Definition definition();

    ApiRequest.Metadata metadata();

    Optional<String> metadata(String key);

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

            boolean isAbout(Point point);

            void effect(Record record, String path, Point point);

            Object configuration();
        }

        enum Point {
            VALIDATE,
            BEFORE_CREATE,
            BEFORE_UPDATE,
            DELIVERY
        }
    }
}
