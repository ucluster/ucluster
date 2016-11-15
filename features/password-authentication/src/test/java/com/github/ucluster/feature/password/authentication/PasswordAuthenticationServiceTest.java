package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.core.User;
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
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();


        Optional<User> user = users.authenticate(request);

        assertThat(user.isPresent(), is(true));
        assertThat(user.get().property("username").get().value(), is("kiwiwin"));
    }

    @Test
    public void should_failed_authenticate_user_when_no_identity_matched() {
        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "noexist")
                        .put("password", "password")
                        .build())
                .get();

        users.authenticate(request);
    }

    @Test
    public void should_failed_authenticate_user_when_password_not_matched() {
        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "wrongpassword")
                        .build())
                .get();

        users.authenticate(request);
    }

    @Test
    public void should_throw_authentication_exception_when_method_not_found() {
        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "nomethod")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "wrongpassword")
                        .build())
                .get();

        users.authenticate(request);
    }
}
