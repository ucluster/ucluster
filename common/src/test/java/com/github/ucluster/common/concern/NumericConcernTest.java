package com.github.ucluster.common.concern;

import com.github.ucluster.common.SimpleRecord;
import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NumericConcernTest {

    private Record.Property.Concern numeric;
    private Record.Property.Concern non_numeric;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        numeric = new NumericConcern("numeric", true);
        non_numeric = new NumericConcern("numeric", false);
    }

    @Test
    public void should_numeric_care_about_validate() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, true)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(numeric.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_non_numeric_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(non_numeric.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_success_validate_valid_numeric() {
        final Record record = builder()
                .path("age").value(18)
                .get();

        numeric.effect(record, "age", Record.Property.Point.VALIDATE);
    }


    @Test
    public void should_failed_validate_non_numeric() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("numeric")
        );

        final Record record = SimpleRecord.builder()
                .path("username").value("kiwi")
                .get();

        numeric.effect(record, "username", Record.Property.Point.VALIDATE);
    }
}