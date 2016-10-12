package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MaskConcernTest {

    private Record.Property.Concern trailing;

    @Before
    public void setUp() throws Exception {
        trailing = new MaskConcern("mask", ImmutableMap.<String, Object>builder()
                .put("trailing", 8)
                .build());
    }

    @Test
    public void should_password_care_about_before_create_and_before_update() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .put(Record.Property.Point.DELIVERY, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(trailing.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_mask_property_trailing() {
        final Record record = builder()
                .path("id_number").value("510108197010101313")
                .get();


        trailing.effect(record, "id_number", Record.Property.Point.DELIVERY);

        final Record.Property idNumber = record.property("id_number").get();
        assertThat(idNumber.value(), is("5101081970********"));
    }
}