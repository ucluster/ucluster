package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
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
    public boolean auto() {
        return false;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        final Optional<Property> value = property("nickname");
        status(Status.APPROVED);
        update();

        user.property("nickname", value.get().value());
        user.update();
    }

    @Override
    public void reject(Map<String, Object> detail) {
        //do nothing
        status(Status.REJECTED);
        update();
    }
}
