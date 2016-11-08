package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.Authentication;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRecord;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.core.authentication.Authentication.Status.FAIL;
import static com.github.ucluster.core.authentication.Authentication.Status.SUCCESS;
import static com.github.ucluster.mongo.Constants.Collection.AUTHENTICATIONS;
import static com.google.common.collect.Maps.newHashMap;

@Entity(AUTHENTICATIONS)
public class MongoAuthentication extends MongoRecord<Authentication> implements Authentication, Model {
    private String type;
    private User user;
    private Status status;

    protected MongoAuthentication() {

    }

    public MongoAuthentication success(User user, Map<String, Object> request) {
        this.user = user;
        this.status = SUCCESS;
        this.loadProperties(request);
        this.loadMetadata(request);
        return this;
    }

    public MongoAuthentication fail(Map<String, Object> request) {
        this.status = FAIL;
        this.save();
        this.loadProperties(request);
        this.loadMetadata(request);
        return this;
    }

    private void loadMetadata(Map<String, Object> request) {
        this.metadata = (Map<String, Object>) request.getOrDefault("metadata", newHashMap());
        this.type = (String) metadata("type").get();
    }

    private void loadProperties(Map<String, Object> request) {
        request.entrySet().forEach(entry -> {
            property(entry.getKey(), entry.getValue());
        });
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public Optional<User> user() {
        return Optional.ofNullable(this.user);
    }

    @Override
    public Status status() {
        return this.status;
    }

    public MongoAuthentication user(User user) {
        this.user = user;
        return this;
    }

    public Authentication build() {
        save();
        return this;
    }
}
