package com.github.ucluster.mongo;

import com.github.ucluster.common.request.RequestBuilder;
import com.github.ucluster.core.ActiveRecord;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.UserAuthenticationException;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(MongoTestRunner.class)
public class MongoUserTest {
    @Inject
    MongoUserRepository users;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final ActiveRecord.Request request = RequestBuilder.of("register")
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("nickname", "kiwinickname")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_user_authenticate_success() {
        user.authenticate(new MongoUserProperty<>("username", "kiwiwin"), new MongoUserProperty<>("password", "password"));
    }

    @Test
    public void should_user_failed_authenticate() {
        thrown.expect(UserAuthenticationException.class);

        user.authenticate(new MongoUserProperty<>("username", "kiwiwin"), new MongoUserProperty<>("password", "invalid_password"));
    }

    @Test
    public void should_user_failed_authenticate_not_using_identity_field() {
        thrown.expect(UserAuthenticationException.class);

        user.authenticate(new MongoUserProperty<>("nickname", "kiwinickname"), new MongoUserProperty<>("password", "password"));
    }
}
