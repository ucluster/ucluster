package com.github.ucluster.feature.passwordless.authentication;

import com.github.ucluster.core.Request;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.request.AuthenticationRequest;
import com.github.ucluster.feature.passwordless.authentication.junit.UClusterFeatureTestRunner;
import com.github.ucluster.mongo.Keys;
import com.github.ucluster.mongo.MongoProperty;
import com.github.ucluster.session.Session;
import com.github.ucluster.test.framework.request.RequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(UClusterFeatureTestRunner.class)
public class PasswordlessAuthenticationServiceTest {

    @Inject
    UserRepository users;

    @Inject
    Session session;

    private User user;

    @Before
    public void setUp() throws Exception {
        final Request request = RequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("phone", "13112341234")
                        .build())
                .request();

        users.create(request);

        user = users.findBy(new MongoProperty<>("username", "kiwiwin")).get();

        session.set(Keys.user_code(user), ImmutableMap.of("confirmation_code", "1234"));
    }

    @Test
    public void should_authenticate_use_the_confirmation_code() throws Exception {
        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "phone")
                        .build()
                )
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("confirmation_code", "1234")
                        .build())
                .request();

        Optional<User> user = users.authenticate(AuthenticationRequest.of(request));
        assertThat(user.isPresent(), is(true));
        assertThat(user.get().property("username").get().value(), is("kiwiwin"));
    }
}