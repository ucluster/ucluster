package com.github.ucluster.core;

import java.util.Map;

public interface User extends ActiveRecord {

    void authenticate(Property identityProperty, Property passwordProperty);

    interface Request {

        String type();

        Map<String, Object> metadata();

        Map<String, Object> properties();

        Map<String, Object> request();
    }
}
