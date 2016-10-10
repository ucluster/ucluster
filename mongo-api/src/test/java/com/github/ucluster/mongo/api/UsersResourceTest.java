package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.junit.ApiSupport;
import com.github.ucluster.mongo.api.junit.ApiTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static com.jayway.jsonpath.JsonPath.read;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(ApiTestRunner.class)
public class UsersResourceTest extends ApiSupport {
    @Test
    public void should_create_user() {
        final Response response = post("users",
                CreateUserRequestBuilder.of("register")
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
                CreateUserRequestBuilder.of("register")
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

        final String json = response.readEntity(String.class);

        assertThat("/users/" + read(json, "$.id"), is(createdResponse.getLocation().getPath()));
        assertThat(read(json, "$.properties.username"), is("kiwiwin"));
        assertThat(read(json, "$.properties.nickname"), is("kiwinickname"));
        assertThat(read(json, "$.properties.email"), is("kiwi.swhite.coder@gmail.com"));
    }
}
