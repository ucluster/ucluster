package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class MongoUserProperty implements User.Property {

    @Property
    private String key;

    @Property
    private String value;

    MongoUserProperty() {
    }

    MongoUserProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }
}
