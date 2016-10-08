package com.github.ucluster.mongo;

import java.util.Map;
import java.util.Optional;

public class UserUpdateNicknameRequest extends MongoRequest {
    UserUpdateNicknameRequest() {
        super();
    }

    UserUpdateNicknameRequest(Map<String, Object> request) {
        super();
        request.entrySet().stream()
                .forEach(e -> {
                    property(new MongoProperty<>(e.getKey(), e.getValue()));
                });
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
