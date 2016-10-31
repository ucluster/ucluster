package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.MongoUserRepository;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.github.ucluster.session.Session;
import com.github.ucluster.test.framework.request.CreateUserRequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static com.github.ucluster.test.framework.matcher.RecordMatcher.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(UClusterTestRunner.class)
public class ID5AsyncRequestTest {
    @Inject
    MongoUserRepository users;

    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
                        .put("nickname", "kiwinickname")
                        .put("email", "kiwi.swhite.coder@gmail.com")
                        .put("password", "password")
                        .build())
                .get();

        user = users.create(request);
    }

    @Test
    public void should_create_pending_id5_async_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .put("id_number", "510108197806101318")
                        .put("id_name", "张三")
                        .build())
                .build());

        final User.Request requestFound = user.request(request.uuid()).get();

        assertThat(requestFound.status(), is(User.Request.Status.PENDING));
        assertThat(requestFound.type(), is("id5_async"));
        assertThat(requestFound.createdAt(), is(notNullValue()));
    }

    @Test
    public void should_failed_to_create_id5_async_request_caused_by_validation_failed() {
        capture(thrown).errors(
                (path, type) -> path.equals("id_number") && type.equals("format")
        );

        user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .put("id_number", "510")
                        .put("id_name", "张三")
                        .build())
                .build());
    }

    @Test
    public void should_success_reject_request_if_change_log_definition_matched() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .put("id_number", "510108197806101318")
                        .put("id_name", "张三")
                        .build())
                .build());

        request.reject(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .put("reason", "not matched")
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.REJECTED));
    }

    @Test
    public void should_success_approve_request() {
        final User.Request request = user.apply(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .put("id_number", "510108197806101318")
                        .put("id_name", "张三")
                        .build())
                .build());

        request.approve(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.<String, Object>builder()
                        .put("type", "id5_async")
                        .build())
                .put("properties", ImmutableMap.<String, String>builder()
                        .build())
                .build());

        assertThat(request.status(), is(User.Request.Status.APPROVED));

        final User.Request requestFound = user.request(request.uuid()).get();
        assertThat(requestFound.status(), is(User.Request.Status.APPROVED));
        assertThat(requestFound.type(), is("id5_async"));

        expect(users.uuid(user.uuid()).get())
                .prop("id_number").value("510108197806101318")
                .prop("id_name").value("张三");
    }
}
