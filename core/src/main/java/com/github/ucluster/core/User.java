package com.github.ucluster.core;

import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface User extends Record {

    User.Request apply(Map<String, Object> request);

    Optional<Request> request(String uuid);

    PaginatedList<Request> requests(Criteria criteria);

    Token generateToken();

    interface Token {
        String accessToken();

        String refreshToken();
    }

    interface Request extends Record {

        String uuid();

        String type();

        Optional<Response> response();

        void execute(Map<String, Object> detail);

        interface Response {

            Collection<Attribute> attributes();

            interface Attribute {

                String key();

                String value();
            }
        }
    }

    interface AuthenticationLog extends Record {
        Optional<User> candidate();

        AuthenticationResponse.Status status();
    }
}
