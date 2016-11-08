package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.Authentication;
import com.github.ucluster.core.authentication.AuthenticationRepository;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(UClusterFeatureTestRunner.class)
public class PasswordAuthenticationServiceTest {
    @Inject
    UserRepository users;

    @Inject
    AuthenticationRepository auth;

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
        auth.authenticate(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of("method", "password", "type", "authentication"))
                .put("username", "kiwiwin")
                .put("password", "password")
                .build()
        );
    }

    @Test
    public void should_failed_authenticate_user_when_no_identity_matched() {

        Authentication authentication = auth.authenticate(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of("method", "password", "type", "authentication"))
                .put("username", "notexist")
                .put("password", "password")
                .build()
        );

        assertThat(authentication.status(), is(Authentication.Status.FAIL));
    }

    @Test
    public void should_failed_authenticate_user_when_password_not_matched() {

        Authentication authentication = auth.authenticate(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of("method", "password", "type", "authentication"))
                .put("username", "kiwiwin")
                .put("password", "wrongpassword")
                .build()
        );

        assertThat(authentication.status(), is(Authentication.Status.FAIL));
    }
}
