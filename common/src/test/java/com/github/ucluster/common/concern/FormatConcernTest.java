package com.github.ucluster.common.concern;

import com.github.ucluster.common.SimpleRecord;
import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.ValidationMatcher.capture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FormatConcernTest {

    private Record.Property.Concern format;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        format = new FormatConcern("format", ImmutableMap.<String, Object>builder()
                .put("pattern", "\\w{6,12}")
                .build());
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
                        assertThat(format.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_get_configuration() {
        final Map<String, Object> configuration = (Map<String, Object>) format.configuration();

        assertThat(configuration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_if_property_is_null() {
        final Record record = SimpleRecord.builder()
                .path("username").none()
                .get();

        format.effect(record, "username");
    }

    @Test
    public void should_success_validate_against_format() {
        final Record record = SimpleRecord.builder()
                .path("username").value("kiwiwin")
                .get();

        format.effect(record, "username");
    }

    @Test
    public void should_failed_to_validate_against_format() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record record = SimpleRecord.builder()
                .path("username").value("kiwi")
                .get();

        format.effect(record, "username");
    }
}
