package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static com.github.ucluster.common.ValidationMatcher.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EmailConcernTest {

    private Record.Property.Concern email;
    private Record.Property.Concern nonEmail;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        email = new EmailConcern("email", true);
        nonEmail = new EmailConcern("email", false);
    }

    @Test
    public void should_password_care_about_validate() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, true)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(email.isAbout(entry.getKey()), is(entry.getValue()))
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
                        assertThat(nonEmail.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_success_validate_valid_email() {
        final Record record = builder()
                .path("email").value("kiwi.swhite.coder@gmail.com")
                .get();

        email.effect(record, "email");
    }

    @Test
    public void should_failed_validate_invalid_email() {
        of(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("email")
        );

        final Record record = builder()
                .path("email").value("invalid.email")
                .get();

        email.effect(record, "email");
    }
}
