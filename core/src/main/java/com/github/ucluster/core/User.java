package com.github.ucluster.core;

import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;

import java.util.Map;
import java.util.Optional;

public interface User extends Record {

    User.Request apply(Map<String, Object> request);

    Optional<Request> request(String requestUuid);

    PaginatedList<Request> requests(Criteria criteria);

    interface Request extends Record {

        String uuid();

        String type();

        Status status();

        boolean autoApprovable();

        void approve(Map<String, Object> detail);

        void reject(Map<String, Object> detail);

        enum Status {
            PENDING,
            APPROVED,
            REJECTED
        }
    }
}
