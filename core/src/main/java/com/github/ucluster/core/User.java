package com.github.ucluster.core;

import java.util.Map;
import java.util.Optional;

public interface User extends Record {

    User.Request apply(Map<String, Object> request);

    Optional<Request> request(String requestUuid);

    interface Request extends Record {

        String uuid();

        String type();

        Status status();

        void approve(Map<String, Object> detail);

        void reject(Map<String, Object> detail);

        enum Status {
            PENDING,
            APPROVED,
            REJECTED
        }
    }
}
