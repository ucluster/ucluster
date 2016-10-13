package com.github.ucluster.mongo;

public interface Constants {
    interface Collection {
        String USERS = "users";
        String REQUESTS = "requests";
        String CHANGE_LOGS = "change_logs";
    }

    interface Record {
        String USER = "user";
        String REQUEST = "request";
        String CHANGE_LOG = "change_log";
    }
}
