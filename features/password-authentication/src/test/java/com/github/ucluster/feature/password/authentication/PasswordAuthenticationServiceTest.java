package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.authentication.AuthenticationRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;
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

import static com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse.Status.SUCCEEDED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(UClusterFeatureTestRunner.class)
public class PasswordAuthenticationServiceTest {
    @Inject
    Repository<User> users;

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

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();


        AuthenticationResponse response = auth.authenticate(request);

        assertThat(response.status(), is(SUCCEEDED));
        assertThat(response.candidate().isPresent(), is(true));
        assertThat(response.candidate().get().property("username").get().value(), is("kiwiwin"));
    }

    @Test
    public void should_failed_authenticate_user_when_no_identity_matched() {

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "noexist")
                        .put("password", "password")
                        .build())
                .get();

        AuthenticationResponse response = auth.authenticate(request);

        assertThat(response.status(), is(FAILED));
        assertThat(response.candidate(), is(Optional.empty()));
    }

    @Test
    public void should_failed_authenticate_user_when_password_not_matched() {

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .put("method", "password")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "wrongpassword")
                        .build())
                .get();

        AuthenticationResponse response = auth.authenticate(request);

        assertThat(response.status(), is(FAILED));
        assertThat(response.candidate().isPresent(), is(true));
        assertThat(response.candidate().get().property("username").get().value(), is("kiwiwin"));
    }

    @Test
    public void should_throw_authentication_exception_when_method_not_found() {

        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "authentication")
                        .put("method", "nomethod")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "wrongpassword")
                        .build())
                .get();

        auth.authenticate(request);
    }
}
