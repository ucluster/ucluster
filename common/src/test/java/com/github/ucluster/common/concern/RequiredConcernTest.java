package com.github.ucluster.common.concern;

import com.github.ucluster.common.SimpleRecord;
import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RequiredConcernTest {

    private Record.Property.Concern required;
    private Record.Property.Concern optional;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        required = new RequiredConcern("required", true);
        optional = new RequiredConcern("required", false);
    }

    @Test
    public void should_required_care_about_validate() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, true)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(required.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }


    @Test
    public void should_optional_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(optional.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_success_required_when_value_presence() {
        final Record record = SimpleRecord.builder()
                .path("username").value("kiwiwin")
                .get();

        required.effect(record, "username", Record.Property.Point.VALIDATE);
    }

    @Test
    public void should_failed_required_but_value_absence() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("required")
        );

        final Record record = SimpleRecord.builder()
                .path("username").none()
                .get();

        required.effect(record, "username", Record.Property.Point.VALIDATE);
    }

    @Test
    public void should_failed_required_but_value_is_null() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("required")
        );

        final Record record = SimpleRecord.builder()
                .path("username").value(null)
                .get();

        required.effect(record, "username", Record.Property.Point.VALIDATE);
    }

    @Test
    public void should_success_optional_when_value_presence() {
        final Record record = SimpleRecord.builder()
                .path("username").value("kiwiwin")
                .get();

        optional.effect(record, "username", Record.Property.Point.VALIDATE);
    }

    @Test
    public void should_success_optional_but_value_absence() {
        final Record record = SimpleRecord.builder()
                .path("username").none()
                .get();

        optional.effect(record, "username", Record.Property.Point.VALIDATE);
    }
}
