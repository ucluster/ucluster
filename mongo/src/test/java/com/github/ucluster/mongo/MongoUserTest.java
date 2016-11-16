package com.github.ucluster.mongo;

import com.github.ucluster.core.Request;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RecordException;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.github.ucluster.session.Session;
import com.github.ucluster.test.framework.request.RequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(UClusterTestRunner.class)
public class MongoUserTest {
    @Inject
    MongoUserRepository users;

    @Inject
    private Session session;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        session.set("confirm:kiwi.swhite.coder@gmail.com", "3102");

        final Request request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("token", "3102")
                        .build())
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("nickname", "kiwinickname")
                        .put("password", "password")
                        .put("email", "kiwi.swhite.coder@gmail.com")
                        .build())
                .request();

        user = users.create(request);
    }

    @Test
    public void should_user_apply_request() {
        user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "update_nickname")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build())
                .build());

        final Optional<User> userFound = users.uuid(user.uuid());
        assertThat(userFound.get().property("nickname").get().value(), is("newnickname"));
    }

    @Test
    public void should_failed_to_apply_request_when_request_validation_failed() {
        capture(thrown).errors(
                (path, type) -> path.equals("nickname") && type.equals("format")
        );

        user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "update_nickname")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "a")
                        .build())
                .build());

        assertThat(user.requests(Criteria.empty()).page(0, 10).isEmpty(), is(true));
    }

    @Test
    public void should_get_all_requests() {
        for (int count = 0; count < 11; count++) {
            users.uuid(user.uuid()).get().apply(ImmutableMap.<String, Object>builder()
                    .put("metadata", ImmutableMap.<String, Object>builder()
                            .put("type", "update_nickname")
                            .build())
                    .put("properties", ImmutableMap.<String, Object>builder()
                            .put("nickname", "newnickname" + count)
                            .build())
                    .build());
        }

        final PaginatedList<User.Request> requests = user.requests(Criteria.empty());

        assertThat(requests.toPage(1, 10).getTotalEntriesCount(), is(11L));
        assertThat(requests.toPage(1, 10).getEntries().size(), is(10));
    }

    @Test
    public void should_not_get_requests_from_other_users() {
        session.set("confirm:kiwiwin@qq1.com", "3102");

        final Request request = RequestBuilder.of()
                .metadata(ImmutableMap.<String, Object>builder()
                        .put("token", "3102")
                        .build())
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "another")
                        .put("password", "another")
                        .put("email", "kiwiwin@qq1.com")
                        .build())
                .request();

        users.create(request);

        final PaginatedList<User.Request> requests = user.requests(Criteria.empty());

        assertThat(requests.toPage(1, 10).getTotalEntriesCount(), is(0L));
        assertThat(requests.toPage(1, 10).getEntries().size(), is(0));
    }

    @Test
    public void should_failed_to_apply_not_supported_request_type() {
        thrown.expect(RecordTypeNotSupportedException.class);

        user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "non_supported")
                        .build())
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build())
                .build());
    }

    @Test
    public void should_handle_concurrent_update_user_property() {
        thrown.expect(RecordException.class);

        IntStream.range(1, 20).parallel().forEach($ -> {
            final User u = users.uuid(this.user.uuid()).get();
            u.property("nickname", "newnickname" + $);
            u.update();
        });
    }
}