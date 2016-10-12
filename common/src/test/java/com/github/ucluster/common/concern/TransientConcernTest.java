package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class TransientConcernTest {

    private Record.Property.Concern enabledTransient;
    private Record.Property.Concern disabledTransient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        enabledTransient = new TransientConcern("transient", true);
        disabledTransient = new TransientConcern("transient", false);
    }

    @Test
    public void should_enabled_transient_care_about_before_create_and_before_update() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, true)
                .put(Record.Property.Point.BEFORE_UPDATE, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(enabledTransient.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_disabled_transient_care_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(disabledTransient.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_effect_property_value_as_null_before_create() {
        final Record record = builder()
                .path("password").value("password")
                .get();

        enabledTransient.effect(record, "password", Record.Property.Point.BEFORE_CREATE);

        assertThat(record.property("password").get().value(), is(nullValue()));
    }

    @Test
    public void should_effect_property_value_as_null_before_update() {
        final Record record = builder()
                .path("password").value("password")
                .get();

        enabledTransient.effect(record, "password", Record.Property.Point.BEFORE_UPDATE);

        assertThat(record.property("password").get().value(), is(nullValue()));
    }
}