package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRecord;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

import static com.github.ucluster.mongo.Constants.Collection.AUTHENTICATIONS;
import static com.google.common.collect.Maps.newHashMap;

@Entity(AUTHENTICATIONS)
public class MongoAuthenticationRequest extends MongoRecord<AuthenticationRequest> implements AuthenticationRequest, Model {

    private String method;

    private AuthenticationResponse response;

    MongoAuthenticationRequest() {

    }

    public MongoAuthenticationRequest(Map<String, Object> request) {
        setMethod(request);
        loadMetadata(request);
        loadProperties(request);
    }

    @Override
    public String method() {
        return this.method;
    }

    @Override
    public void response(AuthenticationResponse response) {
       this.response = response;
    }

    private void setMethod(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        this.method = (String) metadata.getOrDefault("method", "password");
    }

    private void loadMetadata(Map<String, Object> request) {
        this.metadata = (Map<String, Object>) request.getOrDefault("metadata", newHashMap());
    }

    private void loadProperties(Map<String, Object> request) {
        Map<String, Object> properties = (Map<String, Object>) request.getOrDefault("properties", newHashMap());
        properties.entrySet().forEach(entry -> {
            property(entry.getKey(), entry.getValue());
        });
    }
}
