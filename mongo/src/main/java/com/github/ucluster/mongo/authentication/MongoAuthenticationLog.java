package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.User;
import com.github.ucluster.core.authentication.AuthenticationLog;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationResponse.Status;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRecord;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.AUTHENTICATIONS;
import static com.google.common.collect.Maps.newHashMap;

@Entity(AUTHENTICATIONS)
public class MongoAuthenticationLog extends MongoRecord<AuthenticationLog> implements AuthenticationLog, Model {

    @Embedded
    private AuthenticationResponse response;

    public MongoAuthenticationLog(Map<String, Object> request, AuthenticationResponse response) {
        this.response = response;
        loadMetadata(request);
        loadProperties(request);
    }

    private void loadMetadata(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", newHashMap());
        metadata = new HashMap<>(metadata);
        metadata.put("type", "authentication");
        metadata.put("model", "authentication");
        this.metadata = metadata;
    }

    private void loadProperties(Map<String, Object> request) {
        Map<String, Object> properties = (Map<String, Object>) request.getOrDefault("properties", newHashMap());
        properties.entrySet().forEach(entry -> {
            property(entry.getKey(), entry.getValue());
        });
    }

    @Override
    public Optional<User> candidate() {
        return response.candidate();
    }

    @Override
    public Status status() {
        return response.status();
    }
}
