package com.github.ucluster.mongo.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.mongo.junit.UClusterTestRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(UClusterTestRunner.class)
public class MongoAuthenticationRequestFactoryTest {

    @Inject
    private MongoAuthenticationRequestFactory factory;

    @Test
    public void should_create_authentication_request_with_correct_method() throws Exception {

        AuthenticationRequest request = factory.create(ImmutableMap.<String, Object>builder()
                .put("metadata", ImmutableMap.of("method", "password"))
                .put("properties", ImmutableMap.of("username", "kiwiwin", "password", "password"))
                .build());

        assertThat(request.method(), is("password"));
        assertThat(request.property("username").get().value(), is("kiwiwin"));
        assertThat(request.property("password").get().value(), is("password"));
    }
}