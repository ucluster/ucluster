package com.github.ucluster.mongo.api.request;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoRequest;

import java.util.Map;
import java.util.Optional;

public class UpdateNicknameRequest extends MongoRequest {
    UpdateNicknameRequest() {
        super();
    }

    public UpdateNicknameRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public User.Request.Response approve(Map<String, Object> detail) {
        final Optional<Property> value = property("nickname");
        update();

        user.property("nickname", value.get().value());
        user.update();

        return Response.empty();
    }
}
