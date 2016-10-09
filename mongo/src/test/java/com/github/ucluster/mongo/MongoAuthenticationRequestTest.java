package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MongoTestRunner.class)
public class MongoAuthenticationRequestTest {
    @Inject
    MongoUserRepository users;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final Map<String, Object> request = CreateUserRequestBuilder.of("register")
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("nickname", "kiwinickname")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_success_authenticate() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("username", "kiwiwin")
                .put("password", "password")
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_failed_to_authenticate_using_wrong_password() {
        thrown.expect(RequestException.class);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("username", "kiwiwin")
                .put("password", "wrong")
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }
}
