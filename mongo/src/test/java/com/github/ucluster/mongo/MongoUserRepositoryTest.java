package com.github.ucluster.mongo;

import com.github.ucluster.common.request.RequestBuilder;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.UserValidationException;
import com.github.ucluster.mongo.junit.MongoTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

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

    @Inject
    Datastore datastore;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    User user;

    @Before
    public void setUp() throws Exception {
        final User.Request request = RequestBuilder.of("register")
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_failed_to_create_user_if_definition_not_satisfied() {
        thrown.expect(UserValidationException.class);

        final User.Request request = RequestBuilder.of("register")
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_find_user_by_uuid() {
        final User userFound = users.uuid(user.uuid()).get();

        assertThat(userFound.createdAt(), is(notNullValue()));
        assertThat(userFound.property("username").get().value(), is("kiwiwin"));
        assertThat(userFound.property("password").get().value(), is(not("password")));
    }

    @Test
    public void should_find_user_by_property() {
        final Optional<User> userFound = users.find(new MongoUserProperty<>("username", "kiwiwin"));

        assertThat(userFound.isPresent(), is(true));
    }

    @Test
    public void should_update_user_property() {
        final User userBeforeUpdate = users.uuid(user.uuid()).get();

        userBeforeUpdate.update(new MongoUserProperty<>("nickname", "kiwinick"));
        users.update(userBeforeUpdate);

        final User userAfterUpdate = users.uuid(user.uuid()).get();

        assertThat(userAfterUpdate.property("nickname").get().value(), is("kiwinick"));
    }

    @Test
    public void should_failed_to_update_immutable_property() {
        thrown.expect(UserValidationException.class);

        final User userUpdateImmutableProperty = users.uuid(user.uuid()).get();
        userUpdateImmutableProperty.update(new MongoUserProperty<>("username", "anotherkiwi"));

        users.update(userUpdateImmutableProperty);
    }

    @Test
    public void should_failed_to_update_user_if_definition_not_satisfied() {
        thrown.expect(UserValidationException.class);

        final User userBeforeUpdate = users.uuid(user.uuid()).get();

        userBeforeUpdate.update(new MongoUserProperty<>("password", "a"));
        users.update(userBeforeUpdate);
    }

    @Test
    public void should_success_update_password() {
        final User userBeforeUpdate = users.uuid(user.uuid()).get();

        userBeforeUpdate.update(new MongoUserProperty<>("password", "newpassword"));

        users.update(userBeforeUpdate);

        final User userAfterUpdate = users.uuid(this.user.uuid()).get();
        userAfterUpdate.authenticate(
                new MongoUserProperty<>("username", "kiwiwin"),
                new MongoUserProperty<>("password", "newpassword")
        );
    }

    @Test
    public void should_handle_concurrent_update_user_property() {
        final User updateNicknameUser = users.uuid(user.uuid()).get();
        final User updateEmailUser = users.uuid(user.uuid()).get();

        updateNicknameUser.update(new MongoUserProperty<>("nickname", "newnickname"));
        updateEmailUser.update(new MongoUserProperty<>("email", "kiwiwin@gmail.com"));

        users.update(updateNicknameUser);
        users.update(updateEmailUser);

        final User updatedUser = users.uuid(user.uuid()).get();
        assertThat(updatedUser.property("nickname").get().value(), is("newnickname"));
        assertThat(updatedUser.property("email").get().value(), is("kiwiwin@gmail.com"));
    }
}
