package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Map;
import java.util.Optional;

@Entity("user_requests")
public class MongoRequest extends MongoRecord<User.Request> implements User.Request {
    @Reference
    protected User user;

//    @org.mongodb.morphia.annotations.Property
//    protected Status status = Status.PENDING;

    protected MongoRequest() {
    }

    public MongoRequest(User user, Map<String, Object> request) {
        this.user = user;
        request.entrySet().stream()
                .forEach(e -> {
                    property(new MongoProperty<>(e.getKey(), e.getValue()));
                });
    }

    @Override
    public String type() {
        final Optional<Property> type = property("type");
        return (String) type.get().value();
    }

    @Override
    public Status status() {
        final Optional<Property> status = property("status");
        return Status.valueOf((String) status.get().value());
    }

    @Override
    public boolean autoApprovable() {
        throw new RuntimeException("need implemented");
    }

    @Override
    public void approve(Map<String, Object> detail) {
        throw new RuntimeException("need implemented");
    }

    @Override
    public void reject(Map<String, Object> detail) {
        throw new RuntimeException("need implemented");
    }

    protected void status(Status status) {
        property(new MongoProperty<>("status", status.toString()));
    }
}
