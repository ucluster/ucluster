package com.github.ucluster.mongo;

import com.github.ucluster.core.ActiveRecord;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MongoActiveRecord implements ActiveRecord {
    @Id
    protected ObjectId uuid;

    @org.mongodb.morphia.annotations.Property
    protected DateTime createdAt;

    @Embedded
    protected Map<String, Property> properties = new HashMap<>();

    @Override
    public String uuid() {
        return uuid.toHexString();
    }

    @Override
    public DateTime createdAt() {
        return createdAt;
    }

    @Override
    public void update(Property property) {
        properties.put(property.path(), property);
    }

    @Override
    public Optional<Property> property(String propertyPath) {
        return Optional.ofNullable(properties.get(propertyPath));
    }
}
