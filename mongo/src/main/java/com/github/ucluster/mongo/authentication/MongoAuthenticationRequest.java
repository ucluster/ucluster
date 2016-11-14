package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRecord;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.AUTHENTICATIONS;
import static com.google.common.collect.Maps.newHashMap;

@Entity(AUTHENTICATIONS)
public class MongoAuthenticationRequest extends MongoRecord<AuthenticationRequest> implements AuthenticationRequest, Model {

    private String method;

    @Inject
    @Transient
    AuthenticationServiceRegistry registry;

    @Embedded
    private AuthenticationResponse response;

    @Transient
    private Map<String, Object> request;

    public MongoAuthenticationRequest(Map<String, Object> request) {
        this.request = request;
        setMethod(request);
        loadMetadata(request);
        loadProperties(request);
    }

    @Override
    public AuthenticationResponse execute() {
        Optional<AuthenticationService> service = registry.find(method);

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(request);

        audit(response);

        return response;
    }

    private void setMethod(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        this.method = (String) metadata.getOrDefault("method", "password");
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

    private void audit(AuthenticationResponse response) {
        this.response = response;
        save();
    }
}
