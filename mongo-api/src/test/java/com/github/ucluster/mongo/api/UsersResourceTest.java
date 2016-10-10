package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.junit.ApiSupport;
import com.github.ucluster.mongo.api.junit.ApiTestRunner;
import com.github.ucluster.mongo.api.util.CreateUserRequestBuilder;
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
                CreateUserRequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .get()
        );

        assertThat(response.getStatus(), is(201));
    }

    @Test
    public void should_get_user() {
        final Response createdResponse = post("users",
                CreateUserRequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .get()
        );

        final Response response = get(createdResponse.getLocation().getPath());

        assertThat(response.getStatus(), is(200));

        final JsonContext json = json(response);

        assertThat("/users/" + json.path("$.id"), is(createdResponse.getLocation().getPath()));
        assertThat(json.path("$.metadata.model"), is("user"));
        assertThat(json.path("$.metadata.type"), is("default"));

        assertThat(json.path("$.properties.username"), is("kiwiwin"));
        assertThat(json.path("$.properties.nickname"), is("kiwinickname"));
        assertThat(json.path("$.properties.email"), is("kiwi.swhite.coder@gmail.com"));
        assertThat(json.path("$.properties.password"), is(nullValue()));
    }

    @Test
    public void should_get_users() {
        final Response createdResponse = post("users",
                CreateUserRequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .get()
        );

        final Response response = get("/users");

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
