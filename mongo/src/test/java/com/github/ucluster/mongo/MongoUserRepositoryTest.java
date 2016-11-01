package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.github.ucluster.session.Session;
import com.github.ucluster.test.framework.request.CreateUserRequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

@RunWith(UClusterTestRunner.class)
public class MongoUserRepositoryTest {
    @Inject
    UserRepository users;

    @Inject
    Datastore datastore;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    User user;

    @Inject
    private Session session;

    @Before
    public void setUp() throws Exception {
        session.set("confirm:kiwi.swhite.coder@gmail.com", "3102");

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("token", "3102")
                        .build())
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .put("email", "kiwi.swhite.coder@gmail.com")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_failed_to_create_user_when_definition_not_satisfied() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("password", "password")
                        .build())
                .get();

        users.create(request);
    }

    @Test
    public void should_failed_to_create_user_when_missing_property() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("required")
        );

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("password", "password")
                        .build())
                .get();

        users.create(request);
    }

    @Test
    public void should_failed_to_create_user_when_type_not_supported() {
        thrown.expect(RecordTypeNotSupportedException.class);

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("type", "not_supported")
                        .build())
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_failed_to_create_user_when_confirmation_required_but_code_not_match() throws Exception {
        capture(thrown).errors(((path, type) -> path.equals("email") && type.equals("confirm")));

        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("token", "not_match_1102")
                        .build())
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwilose")
                        .put("password", "password")
                        .put("email", "kiwilose@qq.com")
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
        final Optional<User> userFound = users.findBy(new MongoProperty<>("username", "kiwiwin"));

        assertThat(userFound.isPresent(), is(true));
    }

    @Test
    public void should_update_user_property() {
        final User userBeforeUpdate = users.uuid(user.uuid()).get();

        userBeforeUpdate.property("nickname", "kiwinick");
        userBeforeUpdate.update();

        final User userAfterUpdate = users.uuid(user.uuid()).get();
        assertThat(userAfterUpdate.property("nickname").get().value(), is("kiwinick"));
    }

    @Test
    public void should_failed_when_update_immutable_property() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("immutable")
        );

        final User userUpdateImmutableProperty = users.uuid(user.uuid()).get();
        userUpdateImmutableProperty.property("username", "anotherkiwi");

        userUpdateImmutableProperty.update();
    }

    @Test
    public void should_failed_to_update_user_when_definition_not_satisfied() {
        capture(thrown).errors(
                (path, type) -> path.equals("password") && type.equals("format")
        );

        final User userBeforeUpdate = users.uuid(user.uuid()).get();

        userBeforeUpdate.property("password", "a");
        userBeforeUpdate.update();
    }

    @Test
    public void should_find_all_users() {
        for (int count = 0; count < 10; count++) {
            session.set("confirm:kiwi.swhite.coder@gmail.com" + count, "3102");
            users.create(CreateUserRequestBuilder.of()
                    .metadata(ImmutableMap.<String, Object>builder()
                            .put("token", "3102")
                            .build())
                    .properties(ImmutableMap.<String, Object>builder()
                            .put("username", "kiwiwin" + count)
                            .put("password", "password" + count)
                            .put("email", "kiwi.swhite.coder@gmail.com" + count)
                            .build())
                    .get());
        }

        final List<? extends User> found = users.find(Criteria.empty()).page(1, 10);
        assertThat(found.size(), is(10));
    }

    @Test
    public void should_find_by_criteria() {
        for (int count = 0; count < 10; count++) {
            session.set("confirm:kiwi.swhite.coder@gmail.com" + count, "3102");
            users.create(CreateUserRequestBuilder.of()
                    .metadata(ImmutableMap.<String, Object>builder()
                            .put("token", "3102")
                            .build())
                    .properties(ImmutableMap.<String, Object>builder()
                            .put("username", "kiwiwin" + count)
                            .put("password", "password" + count)
                            .put("email", "kiwi.swhite.coder@gmail.com" + count)
                            .build())
                    .get());
        }

        final PaginatedList<User> page = users.find(
                Criteria.params().param("username", "kiwiwin0")
        );

        assertThat(page.toPage(1, 5).getTotalEntriesCount(), is(1L));
        assertThat(page.page(1, 5).size(), is(1));
    }
}
