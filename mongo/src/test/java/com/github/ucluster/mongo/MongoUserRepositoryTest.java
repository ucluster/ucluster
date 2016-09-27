package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

@RunWith(MongoTestRunner.class)
public class MongoUserRepositoryTest {
    @Inject
    UserRepository users;

    User user;

    @Before
    public void setUp() throws Exception {
        user = users.create(ImmutableMap.<String, Object>builder()
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("password", "password").build())
                .build());
    }

    @Test
    public void should_find_user_by_uuid() {
        final User userFound = users.uuid(user.uuid());

        assertThat(userFound.createdAt(), is(notNullValue()));
        assertThat(userFound.property("username").get().value(), is("kiwi"));
        assertThat(userFound.property("password").get().value(), is(not("password")));
    }

    @Test
    public void should_find_user_by_property() {
        final Optional<User> userFound = users.find(new MongoUserProperty("username", "kiwi"));

        assertThat(userFound.isPresent(), is(true));
    }
}
