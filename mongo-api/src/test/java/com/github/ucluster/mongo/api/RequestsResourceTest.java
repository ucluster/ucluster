package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.junit.ApiSupport;
import com.github.ucluster.mongo.api.junit.ApiTestRunner;
import com.github.ucluster.mongo.api.util.CreateUserRequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(ApiTestRunner.class)
public class RequestsResourceTest extends ApiSupport {
    private String userPath;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        userPath = post("users",
                CreateUserRequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .get()
        ).getLocation().getPath();
    }

    @Test
    public void should_create_request() {
        final Response response = post(userPath + "/requests", ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin").build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password").build())
                .build()
        );

        assertThat(response.getStatus(), is(201));
    }

    @Test
    public void should_get_request() {
        final Response createdResponse = post(userPath + "/requests", ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin").build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password").build())
                .build()
        );

        final Response response = get(createdResponse.getLocation().getPath());

        assertThat(response.getStatus(), is(200));

        final JsonContext json = json(response);
        assertThat(json.path("uri"), is(createdResponse.getLocation().getPath()));
        assertThat(json.path("type"), is("authentication"));
        assertThat(json.path("status"), is("APPROVED"));
    }

    @Test
    public void should_not_found_request() {
        final Response response = get(userPath + "/requests/not_exist");

        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_failed_to_approve_non_pending_request() {
        final Response createdResponse = post(userPath + "/requests", ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin").build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password").build())
                .build()
        );

        final Response response = put(createdResponse.getLocation().getPath() + "/approved", ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin").build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password").build())
                .build()
        );

        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void should_get_requests() {
        final Response createdResponse = post(userPath + "/requests", ImmutableMap.<String, Object>builder()
                .put("type", "authentication")
                .put("identity", ImmutableMap.<String, Object>builder()
                        .put("property", "username")
                        .put("value", "kiwiwin").build())
                .put("credential", ImmutableMap.<String, Object>builder()
                        .put("property", "password")
                        .put("value", "password").build())
                .build()
        );

        final Response response = get(userPath + "/requests");

        assertThat(response.getStatus(), is(200));

        final JsonContext json = json(response);

        assertThat(json.path("$.page"), is(1));
        assertThat(json.path("$.total_page"), is(1));
        assertThat(json.path("$.per_page"), is(10));
        assertThat(json.path("$.total_count"), is(1));

        assertThat(json.path("$.entries.length()"), is(1));
        assertThat(json.path("$.entries[0].uri"), is(createdResponse.getLocation().getPath()));
    }
}
