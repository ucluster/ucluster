package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.RecordMock.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ImmutableConcernTest {
    private Record.Property.Concern immutable;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private ImmutableConcern mutable;

    @Before
    public void setUp() throws Exception {
        immutable = new ImmutableConcern("immutable", true);
        mutable = new ImmutableConcern("immutable", false);
    }

    @Test
    public void should_immutable_care_about_before_update_only() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(immutable.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_mutable_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(mutable.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_failed_update_immutable_property() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                return !exception.getEffectResult().valid();
            }
        });

        final Record record = builder()
                .path("email").value("invalid.email")
                .get();

        immutable.effect(record, "email");
    }
}
