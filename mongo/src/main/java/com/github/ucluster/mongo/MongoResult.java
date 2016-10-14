package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.google.common.collect.ImmutableMap;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.RESULTS;

@Entity(RESULTS)
public class MongoResult extends MongoRecord<User.Request.Result> implements User.Request.Result, Model {
    @Reference
    protected User.Request request;

    MongoResult() {
    }

    protected MongoResult(User.Request request, User.Request.Status status) {
        this.request = request;
        property("status", status.toString());
        this.metadata = ImmutableMap.<String, Object>builder()
                .put("model", Constants.Record.RESULT)
                .put("type", request.type())
                .put("action", status == User.Request.Status.APPROVED ? "approve" : "reject")
                .build();
    }

    @Override
    public User.Request.Status status() {
        return statusPropertyOf("status");
    }

    private User.Request.Status statusPropertyOf(String status) {
        final Optional<Property> property = property(status);
        return User.Request.Status.valueOf((String) property.get().value());
    }

    static Builder request(User.Request request) {
        return new Builder(request);
    }

    protected static class Builder {
        private User.Request request;

        Builder(User.Request request) {
            this.request = request;
        }

        MongoResult result(User.Request.Status status) {
            return new MongoResult(request, status);
        }
    }
}
