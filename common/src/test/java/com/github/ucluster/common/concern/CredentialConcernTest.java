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
import static org.hamcrest.Matchers.nullValue;

public class CredentialConcernTest {
    private Record.Property.Concern password;
    private Record.Property.Concern nonPassword;

    @Before
    public void setUp() throws Exception {
        password = new CredentialConcern("password", true);
        nonPassword = new CredentialConcern("password", false);
    }

    @Test
    public void should_credential_care_about_before_create_and_before_update() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, true)
                .put(Record.Property.Point.BEFORE_UPDATE, true)
                .put(Record.Property.Point.DELIVERY, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(password.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_non_credential_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .put(Record.Property.Point.DELIVERY, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(nonPassword.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_encrypt_credential() {
        final Record record = builder()
                .path("password").value("password")
                .get();

        password.effect(record, "password", Record.Property.Point.VALIDATE);

        final String encrypted = (String) record.property("password").get().value();
        assertThat(encrypted, not("password"));
    }

    @Test
    public void should_not_delivery_credential() {
        final Record record = builder()
                .path("password").value("password")
                .get();

        password.effect(record, "password", Record.Property.Point.DELIVERY);

        assertThat(record.property("password").get().value(), is(nullValue()));
    }
}
