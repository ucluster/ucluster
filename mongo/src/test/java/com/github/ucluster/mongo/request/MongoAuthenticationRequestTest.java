package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.CreateUserRequestBuilder;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(UClusterTestRunner.class)
public class MongoAuthenticationRequestTest {
    @Inject
    MongoUserRepository users;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("nickname", "kiwinickname")
                        .put("email", "kiwi.swhite.coder@gmail.com")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_success_authenticate_using_username() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin")
                        .build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));

        final List<User.Request.ChangeLog> changeLogs = request.changeLogs();
        assertThat(changeLogs.size(), is(1));

        assertThat(changeLogs.get(0).oldStatus(), is(User.Request.Status.PENDING));
        assertThat(changeLogs.get(0).newStatus(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_failed_to_authenticate_using_username_with_wrong_password() {
        thrown.expect(RequestException.class);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin")
                        .build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "wrong")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }

    @Test
    public void should_success_to_authenticate_using_identity_field() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "email")
                        .put("value", "kiwi.swhite.coder@gmail.com")
                        .build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_failed_to_authenticate_using_non_identity_field() {
        thrown.expect(RequestException.class);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "nickname")
                        .put("value", "kiwinickname")
                        .build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }
}
