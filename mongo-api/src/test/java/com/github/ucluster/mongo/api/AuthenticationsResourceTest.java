package com.github.ucluster.mongo.api;

import com.github.ucluster.mongo.api.junit.ApiSupport;
import com.github.ucluster.mongo.api.junit.ApiTestRunner;
import com.github.ucluster.test.framework.request.RequestBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(ApiTestRunner.class)
public class AuthenticationsResourceTest extends ApiSupport {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        post("users",
                RequestBuilder.of()
                        .properties(ImmutableMap.<String, Object>builder()
                                .put("username", "kiwiwin")
                                .put("nickname", "kiwinickname")
                                .put("email", "kiwi.swhite.coder@gmail.com")
                                .put("password", "password")
                                .build())
                        .get()
        );
    }

    @Test
    public void should_authenticate_use_username_and_password() throws Exception {
        Response response = post("authentications", ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of(
                        "type", "authentication",
                        "method", "password",
                        "model", "authentication"))
                .put("username", "kiwiwin")
                .put("password", "password")
                .build()
        );
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void should_authenticate_fail_use_username_and_password() throws Exception {
        Response response = post("authentications", ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of(
                        "type", "authentication",
                        "method", "password",
                        "model", "authentication"))
                .put("username", "kiwiwin")
                .put("password", "wrong")
                .build()
        );
        assertThat(response.getStatus(), is(401));
    }
}
