package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.github.ucluster.session.Session;
import com.github.ucluster.test.framework.request.CreateUserRequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(UClusterTestRunner.class)
public class AuthenticationRequestTest {
    @Inject
    MongoUserRepository users;

    @Inject
    Session session;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        session.set("confirm:kiwi.swhite.coder@gmail.com", "3102");

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("token", "3102")
                        .build())
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
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("identity_property", "username")
                        .put("identity_value", "kiwiwin")
                        .put("credential_property", "password")
                        .put("credential_value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));
        final User.Request.Response response = request.response().get();
        final List<User.Request.Response.Attribute> attributes = new ArrayList<>(response.attributes());
        assertThat(attributes.size(), is(1));
        assertThat(attributes.get(0).key(), is("$TOKEN"));
        assertThat(session.hgetall(attributes.get(0).value()), is(notNullValue()));
    }

    @Test
    public void should_failed_to_authenticate_using_username_with_wrong_password() {
        thrown.expect(RequestException.class);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("identity_property", "username")
                        .put("identity_value", "kiwiwin")
                        .put("credential_property", "password")
                        .put("credential_value", "wrong")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }

    @Test
    public void should_success_to_authenticate_using_identity_field() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("identity_property", "email")
                        .put("identity_value", "kiwi.swhite.coder@gmail.com")
                        .put("credential_property", "password")
                        .put("credential_value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_failed_to_authenticate_using_non_identity_field() {
        thrown.expect(RequestException.class);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("identity_property", "nickname")
                        .put("identity_value", "kiwinickname")
                        .put("credential_property", "password")
                        .put("credential_value", "password")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }
}
