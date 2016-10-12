package com.github.ucluster.mongo;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestTypeNotSupportException;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(UClusterTestRunner.class)
public class MongoUserTest {
    @Inject
    MongoUserRepository users;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin")
                        .put("nickname", "kiwinickname")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_user_apply_auto_approvable_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "auto_approvable")
                        .build()
                )
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build()
                ).build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));

        final Optional<User> userFound = users.uuid(user.uuid());
        assertThat(userFound.get().property("nickname").get().value(), is("newnickname"));

        final Optional<User.Request> requestFound = user.request(request.uuid());
        assertThat(requestFound.get().status(), is(User.Request.Status.APPROVED));
    }

    @Test
    public void should_user_apply_non_auto_approvable_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "non_auto_approvable")
                        .build()
                )
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build()
                ).build());

        assertThat(request.status(), is(User.Request.Status.PENDING));

        final User userFound = users.uuid(user.uuid()).get();
        assertThat(userFound.property("nickname").get().value(), is("kiwinickname"));
    }

    @Test
    public void should_user_apply_non_auto_approvable_request_and_approve_it_later() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "non_auto_approvable")
                        .build()
                )
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build()
                ).build());

        final User userFound = users.uuid(user.uuid()).get();

        final User.Request pendingRequest = userFound.request(request.uuid()).get();
        pendingRequest.approve(new HashMap<>());
        assertThat(pendingRequest.status(), is(User.Request.Status.APPROVED));

        final User.Request approvedRequest = userFound.request(request.uuid()).get();
        assertThat(approvedRequest.status(), is(User.Request.Status.APPROVED));

        final User userAfterApproved = users.uuid(user.uuid()).get();
        assertThat(userAfterApproved.property("nickname").get().value(), is("newnickname"));
    }

    @Test
    public void should_user_apply_non_auto_approvable_request_and_reject_it_later() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "non_auto_approvable")
                        .build()
                )
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build()
                ).build());

        final User userFound = users.uuid(user.uuid()).get();

        final User.Request pendingRequest = userFound.request(request.uuid()).get();
        pendingRequest.reject(new HashMap<>());
        assertThat(pendingRequest.status(), is(User.Request.Status.REJECTED));

        final User.Request rejectedRequest = userFound.request(request.uuid()).get();
        assertThat(rejectedRequest.status(), is(User.Request.Status.REJECTED));

        final User userAfterRejected = users.uuid(user.uuid()).get();
        assertThat(userAfterRejected.property("nickname").get().value(), is("kiwinickname"));
    }

    @Test
    public void should_get_all_requests() {
        for (int count = 0; count < 11; count++) {
            user.apply(ImmutableMap.<String, Object>builder()
                    .put("metadata", ImmutableMap.<String, Object>builder()
                            .put("type", "non_auto_approvable")
                            .build()
                    )
                    .put("properties", ImmutableMap.<String, Object>builder()
                            .put("nickname", "newnickname")
                            .build()
                    ).build());
        }

        final PaginatedList<User.Request> requests = user.requests(Criteria.empty());

        assertThat(requests.toPage(1, 10).getTotalEntriesCount(), is(11L));
        assertThat(requests.toPage(1, 10).getEntries().size(), is(10));
    }

    @Test
    public void should_not_get_requests_from_other_users() {
        final Map<String, Object> request = CreateUserRequestBuilder.of()
                .properties(ImmutableMap.<String, Object>builder()
                        .put("username", "another")
                        .put("password", "another")
                        .build())
                .get();

        users.create(request);

        final PaginatedList<User.Request> requests = user.requests(Criteria.empty());

        assertThat(requests.toPage(1, 10).getTotalEntriesCount(), is(0L));
        assertThat(requests.toPage(1, 10).getEntries().size(), is(0));
    }

    @Test
    public void should_failed_to_apply_not_supported_request_type() {
        thrown.expect(RequestTypeNotSupportException.class);

        user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "non_supported")
                        .build()
                )
                .put("properties", ImmutableMap.<String, Object>builder()
                        .put("nickname", "newnickname")
                        .build()
                ).build());

    }
}
