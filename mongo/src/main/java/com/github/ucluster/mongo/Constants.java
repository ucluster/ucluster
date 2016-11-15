package com.github.ucluster.mongo;

public interface Constants {
    interface Collection {
        String USERS = "users";
        String REQUESTS = "requests";
        String AUTHENTICATIONS = "authentications";
    }

    interface Record {
        String USER = "user";
        String REQUEST = "request";
        String AUTHENTICATION = "authentication";
    }

    interface Token {
        Integer ACCESS_EXPIRE_SECONDS = 30 * 60;
        Integer REFRESH_EXPIRE_SECONDS = 7 * 24 * 60 * 60;
    }
}
