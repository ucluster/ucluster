package com.github.ucluster.core;

import com.github.ucluster.core.definition.Definition;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Optional;

public interface Record {

    String uuid();

    DateTime createdAt();

    void property(Property property);

    Optional<Property> property(String propertyPath);

    Collection<Property> properties();

    Definition definition();

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

            void effect(Record record, String propertyPath);

            Object configuration();
        }

        enum Point {
            VALIDATE,
            BEFORE_CREATE,
            BEFORE_UPDATE
        }
    }

}
