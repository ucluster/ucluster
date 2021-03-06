package com.github.ucluster.mongo.api.request;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoRequest;

import java.util.Optional;

public class UpdateNicknameRequest extends MongoRequest {
    UpdateNicknameRequest() {
        super();
    }

    public UpdateNicknameRequest(User user, ApiRequest request) {
        super(user, request);
    }

    @Override
    public void execute(ApiRequest request) {
        final Optional<Property> value = property("nickname");
        update();

        user.property("nickname", value.get().value());
        user.update();
    }
}
