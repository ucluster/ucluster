package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.UserValidationException;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(MongoTestRunner.class)
public class MongoUserTest {
    @Inject
    UserRepository users;

    private User user;

    @Before
    public void setUp() throws Exception {
        user = users.create(ImmutableMap.<String, Object>builder()
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("password", "password").build())
                .build());
    }

    @Test
    public void should_user_authenticate_success() {
        user.authenticate(new MongoUserProperty("username", "kiwi"), "password");
    }

    @Test(expected = UserValidationException.class)
    public void should_user_failed_authenticate() {
        user.authenticate(new MongoUserProperty("username", "kiwi"), "invalid_password");
    }
}
