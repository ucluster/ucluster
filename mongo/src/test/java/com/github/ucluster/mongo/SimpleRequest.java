package com.github.ucluster.mongo;

import com.github.ucluster.core.User;

import java.util.Map;
import java.util.Optional;

public class SimpleRequest extends MongoRequest {
    SimpleRequest() {
        super();
    }

    public SimpleRequest(User user, Map<String, Object> request) {
        super(user, request);
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
