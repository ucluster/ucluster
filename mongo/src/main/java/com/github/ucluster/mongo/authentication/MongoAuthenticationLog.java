package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.User;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationResponse.Status;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRecord;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.AUTHENTICATIONS;

@Entity(AUTHENTICATIONS)
public class MongoAuthenticationLog extends MongoRecord<User.AuthenticationLog> implements User.AuthenticationLog, Model {

    @Embedded
    private AuthenticationResponse response;

    public MongoAuthenticationLog(ApiRequest request, AuthenticationResponse response) {
        this.response = response;
        request.model("authentication");
        this.metadata = request.metadata();
        loadProperties(request);
    }

    private void loadProperties(ApiRequest request) {
        request.properties().entrySet().forEach(entry -> {
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
