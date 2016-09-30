package com.github.ucluster.mongo;

import com.github.ucluster.core.ActiveRecord;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class MongoUserProperty<T> implements ActiveRecord.Property<T> {

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

    static String valueMongoField(ActiveRecord.Property property) {
        return mongoField(property) + ".value";
    }

    static String mongoField(ActiveRecord.Property property) {
        return "properties." + property.path();
    }
}
