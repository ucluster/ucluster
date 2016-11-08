package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.feature.password.authentication.junit.UClusterFeatureTestRunner;
import com.github.ucluster.test.framework.request.RequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

@RunWith(UClusterFeatureTestRunner.class)
public class PasswordAuthenticationServiceTest {
    @Inject
    UserRepository users;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final Map<String, Object> request = RequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();

        users.create(request);
    }

    @Test
    public void should_success_authenticate_user() {
        users.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("password", "password")
                .build()
        );
    }

    @Test
    public void should_failed_authenticate_user_when_no_identity_matched() {
        thrown.expect(AuthenticationException.class);

        users.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "notexist")
                .put("password", "password")
                .build()
        );
    }

    @Test
    public void should_failed_authenticate_user_when_password_not_matched() {
        thrown.expect(AuthenticationException.class);

        users.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("password", "wrongpassword")
                .build()
        );
    }
}
