package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.junit.ApiSupport;
import com.github.ucluster.mongo.api.junit.ApiTestRunner;
import com.github.ucluster.test.framework.json.JsonContext;
import com.github.ucluster.test.framework.request.RequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(ApiTestRunner.class)
public class UsersResourceTest extends ApiSupport {
    @Test
    public void should_create_user() {
        final Response response = post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .request()
        );

        assertThat(response.getStatus(), is(201));
    }

    @Test
    public void should_failed_to_create_user_with_duplicate_identity_property() {
        post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("password", "password")
                                .build())
                        .request()
        );

        final Response response = post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("password", "password")
                                .build())
                        .request()
        );

        assertThat(response.getStatus(), is(400));

        final JsonContext json = JsonContext.json(response);

        assertThat(json.path("$.errors.length()"), is(1));
        assertThat(json.path("$.errors[0].property"), is("username"));
        assertThat(json.path("$.errors[0].cause"), is("identity"));
    }

    @Test
    public void should_failed_to_create_user_with_illegal_property() {
        final Response response = post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "k")
                                .put("password", "p")
                                .build())
                        .request()
        );

        assertThat(response.getStatus(), is(400));

        final JsonContext json = JsonContext.json(response);

        assertThat(json.path("$.errors.length()"), is(2));
        assertThat(json.path("$.errors[0].property"), is("password"));
        assertThat(json.path("$.errors[0].cause"), is("format"));

        assertThat(json.path("$.errors[1].property"), is("username"));
        assertThat(json.path("$.errors[1].cause"), is("format"));
    }

    @Test
    public void should_failed_to_create_user_with_not_suppported_type() {
        final Response response = post("users",
                RequestBuilder.of()
                        .metadata(ImmutableMap.<String, Object>builder()
                                .put("user_type", "unsupported")
                                .build())
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("password", "password")
                                .build())
                        .request()
        );

        assertThat(response.getStatus(), is(400));

        final JsonContext json = JsonContext.json(response);

        assertThat(json.path("$.errors.length()"), is(1));
        assertThat(json.path("$.errors[0].cause"), is("unsupported.type"));
        assertThat(json.path("$.errors[0].type"), is("unsupported"));
    }

    @Test
    public void should_get_user() {
        final Response createdResponse = post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .request()
        );

        final Response response = get(createdResponse.getLocation().getPath());

        assertThat(response.getStatus(), is(200));

        final JsonContext json = JsonContext.json(response);

        assertThat("/users/" + json.path("$.id"), is(createdResponse.getLocation().getPath()));
        assertThat(json.metadata("model"), is("user"));
        assertThat(json.metadata("user_type"), is("default"));

        assertThat(json.property("username"), is("kiwiwin"));
        assertThat(json.property("nickname"), is("kiwinickname"));
        assertThat(json.property("email"), is("kiwi.swhite.coder@gmail.com"));
        assertThat(json.property("password"), is(nullValue()));
    }

    @Test
    public void should_not_found_user() {
        final Response response = get("/users/not_exist");

        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_get_users() {
        final Response createdResponse = post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .request()
        );

        final Response response = get("/users");

        assertThat(response.getStatus(), is(200));

        final JsonContext json = JsonContext.json(response);

        assertThat(json.path("$.page"), is(1));
        assertThat(json.path("$.total_page"), is(1));
        assertThat(json.path("$.per_page"), is(10));
        assertThat(json.path("$.total_count"), is(1));

        assertThat(json.path("$.entries.length()"), is(1));
        assertThat(json.path("$.entries[0].uri"), is(createdResponse.getLocation().getPath()));
    }
}
