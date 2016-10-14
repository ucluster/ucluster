package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.google.common.collect.ImmutableMap;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.CHANGE_LOGS;

@Entity(CHANGE_LOGS)
public class MongoChangeLog extends MongoRecord<User.Request.ChangeLog> implements User.Request.ChangeLog, Model {
    @Reference
    protected User.Request request;

    MongoChangeLog() {
    }

    public MongoChangeLog(User.Request request, User.Request.Status oldStatus, User.Request.Status newStatus) {
        this.request = request;
        property("old_status", oldStatus.toString());
        property("new_status", newStatus.toString());
    }

    @Override
    public User.Request.Status oldStatus() {
        return statusPropertyOf("old_status");
    }

    @Override
    public User.Request.Status newStatus() {
        return statusPropertyOf("new_status");
    }

    private User.Request.Status statusPropertyOf(String status) {
        final Optional<Property> property = property(status);
        return User.Request.Status.valueOf((String) property.get().value());
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("id", uuid());
        json.put("created_at", createdAt());
        json.put("old_status", oldStatus());
        json.put("new_status", newStatus());

        return json;
    }

    @Override
    public Map<String, Object> toReferenceJson() {
        return toJson();
    }

    static Builder of(User.Request request) {
        return new Builder(request);
    }

    protected static class Builder {
        private User.Request request;
        private User.Request.Status oldStatus;

        Builder(User.Request request) {
            this.request = request;
        }

        Builder from(User.Request.Status oldStatus) {
            this.oldStatus = oldStatus;
            return this;
        }

        MongoChangeLog to(User.Request.Status newStatus) {
            final MongoChangeLog changeLog = new MongoChangeLog(request, oldStatus, newStatus);

            changeLog.metadata = ImmutableMap.<String, Object>builder()
                    .put("model", Constants.Record.CHANGE_LOG)
                    .put("type", request.type())
                    .put("action", newStatus == User.Request.Status.APPROVED ? "approve" : "reject")
                    .build();

            return changeLog;
        }
    }
}
