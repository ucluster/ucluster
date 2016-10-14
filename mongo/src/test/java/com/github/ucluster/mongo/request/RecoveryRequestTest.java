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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(UClusterTestRunner.class)
public class RecoveryRequestTest {
    @Inject
    MongoUserRepository users;

    @Inject
    Session session;

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
    public void should_success_recover_credential_when_ott_matched() {
        session.setex(user.uuid() + ":ott", "123456", 1);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("ott", "123456")
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "recovery")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("credential_property", "password")
                        .put("credential_value", "recovered")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));

        final User.Request authRequest = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("identity_property", "username")
                        .put("identity_value", "kiwiwin")
                        .put("credential_property", "password")
                        .put("credential_value", "recovered")
                        .build())
                .build());

        assertThat(authRequest.status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_failed_to_recover_credential_when_ott_not_matched() {
        thrown.expect(RequestException.class);

        session.setex(user.uuid() + ":ott", "654321", 1);

        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("ott", "123456")
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "recovery")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("credential_property", "password")
                        .put("credential_value", "recovered")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));

        final User.Request authRequest = user.apply(ImmutableMap.<String, Object>builder()
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

        assertThat(authRequest.status(), is(User.Request.Status.APPROVED));
    }
}
