package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoProperty;
import com.github.ucluster.mongo.MongoRequest;

import java.util.Map;
import java.util.Optional;

public class NonAutoApprovableRequest extends MongoRequest {
    NonAutoApprovableRequest() {
        super();
    }

    public NonAutoApprovableRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean autoApprovable() {
        return false;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        final Optional<Property> value = property("nickname");
        user.property(new MongoProperty<>("nickname", value.get().value()));
        user.update();
        status(Status.APPROVED);
    }

    @Override
    public void reject(Map<String, Object> detail) {
        //do nothing
        status(Status.REJECTED);
    }
}