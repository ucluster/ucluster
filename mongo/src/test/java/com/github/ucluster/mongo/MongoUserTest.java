package com.github.ucluster.mongo;

import com.github.ucluster.common.request.RequestBuilder;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.UserAuthenticationException;
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
        final User.Request request = RequestBuilder.of("register")
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_user_authenticate_success() {
        user.authenticate(new MongoUserProperty<>("username", "kiwiwin"), new MongoUserProperty<>("password", "password"));
    }

    @Test(expected = UserAuthenticationException.class)
    public void should_user_failed_authenticate() {
        user.authenticate(new MongoUserProperty<>("username", "kiwiwin"), new MongoUserProperty<>("password", "invalid_password"));
    }
}
