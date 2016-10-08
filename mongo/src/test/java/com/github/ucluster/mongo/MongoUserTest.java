package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MongoTestRunner.class)
public class MongoUserTest {
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
    public void should_user_apply_auto_approvable_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "auto_approvable")
                .put("nickname", "newnickname").build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));

        final Optional<User> userFound = users.uuid(user.uuid());
        assertThat(userFound.get().property("nickname").get().value(), is("newnickname"));

        final Optional<User.Request> requestFound = user.request(request.uuid());
        assertThat(requestFound.get().status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_user_apply_non_auto_approvable_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "non_auto_approvable")
                .put("nickname", "newnickname").build());

        assertThat(request.status(), is(User.Request.Status.PENDING));
    }
}
