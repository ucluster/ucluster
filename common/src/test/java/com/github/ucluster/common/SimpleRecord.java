package com.github.ucluster.common;

import com.github.ucluster.core.Record;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SimpleRecord implements Record {
    private List<Record.Property> properties = new ArrayList<>();

    public SimpleRecord() {
    }

    public static SimpleRecord builder() {
        return new SimpleRecord();
    }

    @Override
    public String uuid() {
        return "uuid-fixed";
    }

    @Override
    public DateTime createdAt() {
        return new DateTime();
    }

    @Override
    public void update(Property property) {

    }

    public SimpleRecord.FieldBuilder path(String path) {
        return new FieldBuilder(path);
    }

    public Record get() {
        return this;
    }

    @Override
    public Optional<Property> property(String propertyPath) {
        return properties().stream().filter(property -> property.path().equals(propertyPath)).findAny();
    }

    @Override
    public Collection<Property> properties() {
        return properties;
    }

    public class FieldBuilder {
        private final String path;

        FieldBuilder(String path) {
            this.path = path;
        }

        public <T> SimpleRecord value(T value) {
            SimpleRecord.this.properties.add(new Property<T>() {
                private T v = value;

                @Override
                public String path() {
                    return path;
                }

                @Override
                public T value() {
                    return v;
                }

                @Override
                public void value(T value) {
                    v = value;
                }
            });

            return SimpleRecord.this;
        }

        public SimpleRecord none() {
            return SimpleRecord.this;
        }
    }
}
