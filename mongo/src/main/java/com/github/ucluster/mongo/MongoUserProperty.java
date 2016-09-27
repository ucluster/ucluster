package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class MongoUserProperty<T> implements User.Property<T> {

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
    public String key() {
        return key;
    }

    @Override
    public T value() {
        return value;
    }
}
