package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Optional;

@Entity("user_requests")
@Converters(JodaDateTimeConverter.class)
public abstract class MongoRequest extends MongoRecord<User.Request> implements User.Request {
    @Reference
    protected User user;

    @org.mongodb.morphia.annotations.Property
    protected Status status;

    protected MongoRequest() {
        status(Status.PENDING);
    }

    @Override
    public String type() {
        final Optional<Property> type = property("type");
        return (String) type.get().value();
    }

    @Override
    public Status status() {
        return status;
    }

    protected void status(Status status) {
        this.status = status;
    }
}
