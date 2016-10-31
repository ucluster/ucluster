package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoRequest;

import java.util.Map;
import java.util.Optional;

public class AutoApprovableRequest extends MongoRequest {
    AutoApprovableRequest() {
        super();
    }

    public AutoApprovableRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean auto() {
        return true;
    }

    @Override
    public User.Request.Response approve(Map<String, Object> detail) {
        final Optional<Property> value = property("nickname");
        status(Status.APPROVED);
        update();

        user.property("nickname", value.get().value());
        user.update();

        return Response.empty();
    }

}
