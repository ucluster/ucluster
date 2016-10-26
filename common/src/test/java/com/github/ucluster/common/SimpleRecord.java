package com.github.ucluster.common;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import org.joda.time.DateTime;

import java.util.*;

public class SimpleRecord implements Record {
    private List<Record.Property> properties = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();

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
    public void property(Property property) {
        property(property.path(), property.value());
    }

    @Override
    public <V> void property(String path, V value) {
        properties().stream().filter(prop -> prop.path().equals(path)).forEach(prop -> prop.value(value));
    }

    public SimpleRecord.FieldBuilder path(String path) {
        return new FieldBuilder(path);
    }

    public Record get() {
        return this;
    }

    @Override
    public Optional<Property> property(String path) {
        return properties().stream().filter(property -> property.path().equals(path)).findAny();
    }

    @Override
    public Collection<Property> properties() {
        return properties;
    }

    @Override
    public Definition definition() {
        return null;
    }

    @Override
    public Optional<Object> metadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    public SimpleRecord withMetadata(String key, Object value) {
        metadata.put(key, value);
        return this;
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
