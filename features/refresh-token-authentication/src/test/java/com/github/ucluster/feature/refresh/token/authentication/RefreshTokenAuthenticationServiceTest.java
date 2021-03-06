package com.github.ucluster.feature.refresh.token.authentication;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.feature.refresh.token.junit.UClusterFeatureTestRunner;
import com.github.ucluster.mongo.MongoProperty;
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
public class RefreshTokenAuthenticationServiceTest {
    @Inject
    UserRepository users;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User.Token token;

    @Before
    public void setUp() throws Exception {
        final ApiRequest request = RequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .request();

        users.create(request);

        token = users.findBy(new MongoProperty<>("username", "kiwiwin")).get().generateToken();
    }

    @Test
    public void should_success_authenticate_user() throws Exception {
        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "refresh")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("access_token", token.accessToken())
                        .put("refresh_token", token.refreshToken())
                        .build())
                .request();

        Optional<User> user = users.authenticate(ApiRequest.of(request));
        assertThat(user.isPresent(), is(true));
        assertThat(user.get().property("username").get().value(), is("kiwiwin"));
    }

    @Test
    public void should_failed_authenticate_user_when_refresh_token_invalid() {
        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "refresh")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("access_token", "invalid")
                        .put("refresh_token", "invalid")
                        .build())
                .request();

        users.authenticate(ApiRequest.of(request));
    }

    @Test
    public void should_failed_authenticate_user_when_refresh_token_and_authentication_token_not_matched() {
        thrown.expect(AuthenticationException.class);

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "refresh")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("access_token", "invalid")
                        .put("refresh_token", token.refreshToken())
                        .build())
                .request();

        users.authenticate(ApiRequest.of(request));
    }
}