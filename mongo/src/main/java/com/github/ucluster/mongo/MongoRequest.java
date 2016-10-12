package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity("user_requests")
public class MongoRequest extends MongoRecord<User.Request> implements User.Request, Model {
    @Reference
    protected User user;

    protected MongoRequest() {
    }

    public MongoRequest(User user, Map<String, Object> request) {
        this.user = user;
        status(Status.PENDING);
        loadMetadata(request);
        loadProperties(request);
    }

    private void loadMetadata(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", new HashMap<>());
        //for immutable map
        metadata = new HashMap<>(metadata);
        metadata.put("model", "request");

        this.metadata = metadata;
    }

    private void loadProperties(Map<String, Object> request) {
        ((Map<String, Object>) request.get("properties")).entrySet().stream()
                .forEach(e -> {
                    property(new MongoProperty<>(e.getKey(), e.getValue()));
                });
    }

    @Override
    public String type() {
        return (String) metadata.get("type");
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

    @Override
    public List<ChangeLog> changeLogs() {
        return datastore.createQuery(MongoChangeLog.class)
                .disableValidation()
                .field("request").equal(new Key<>(MongoRequest.class, "user_requests", uuid))
                .order("-createdAt")
                .asList().stream()
                .collect(Collectors.toList());
    }

    protected void status(Status status, Property... properties) {
        if (status != Status.PENDING) {
            recordChangeLog(status, properties);
        }
        property(new MongoProperty<>("status", status.toString()));
    }

    protected void recordChangeLog(Status newStatus, Property... properties) {
        final MongoChangeLog changeLog = MongoChangeLog.of(this).from(status()).to(newStatus);
        for (Property property : properties) {
            changeLog.property(property);
        }

        enhance(changeLog);
        changeLog.save();
    }

    private void enhance(MongoChangeLog changeLog) {
        changeLog.request = this;
        injector.injectMembers(changeLog);
    }

    protected void ensurePending() {
        if (status() != Status.PENDING) {
            throw new RequestException();
        }
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("id", uuid());
        json.put("uri", Routing.request(user, this));
        json.put("created_at", createdAt());
        json.put("type", type());
        json.put("status", status());

        return json;
    }

    @Override
    public Map<String, Object> toReferenceJson() {
        return toJson();
    }
}
