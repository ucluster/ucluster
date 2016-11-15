package com.github.ucluster.feature.passwordless.authentication.token;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.feature.passwordless.authentication.junit.UClusterFeatureTestRunner;
import com.github.ucluster.mongo.MongoProperty;
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
public class RefreshTokenAuthenticationServiceTest {

    @Inject
    UserRepository users;

    private Map<String, Object> token;
    private User user;

    @Before
    public void setUp() throws Exception {
        final Map<String, Object> request = RequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();

        users.create(request);

        user = users.findBy(new MongoProperty<>("username", "kiwiwin")).get();

        token = user.generateToken();
    }

    @Test
    public void should_get_a_new_token_along_with_new_refresh_token() throws Exception {

        Map<String, Object> request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("method", "refresh")
                        .build()
                )
                .properties(token)
                .get();

        Optional<User> user = users.authenticate(request);
        assertThat(user.isPresent(), is(true));
        assertThat(user.get().property("username").get().value(), is("kiwiwin"));
    }
}