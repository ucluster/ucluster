package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class MongoUserProperty<T> implements Record.Property<T> {

    @Property
    private String key;

    @Property
    private T value;

    MongoUserProperty() {
    }

    MongoUserProperty(String key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String path() {
        return key;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public void value(T value) {
        this.value = value;
    }

    static String valueMongoField(Record.Property property) {
        return mongoField(property) + ".value";
    }

    static String mongoField(Record.Property property) {
        return "properties." + property.path();
    }
}
