package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CredentialConcernTest {
    private Record.Property.Concern password;
    private Record.Property.Concern nonPassword;

    @Before
    public void setUp() throws Exception {
        password = new CredentialConcern("password", true);
        nonPassword = new CredentialConcern("password", false);
    }

    @Test
    public void should_password_care_about_before_create_and_before_update() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, true)
                .put(Record.Property.Point.BEFORE_UPDATE, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(password.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_non_password_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(nonPassword.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_encrypt_password() {
        final Record record = builder()
                .path("password").value("password")
                .get();

        password.effect(record, "password");

        final String encrypted = (String) record.property("password").get().value();
        assertThat(encrypted, not("password"));
    }
}
