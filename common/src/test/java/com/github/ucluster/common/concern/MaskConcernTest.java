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
    private Record.Property.Concern leading;
    private Record.Property.Concern range;

    @Before
    public void setUp() throws Exception {
        trailing = new MaskConcern("mask", ImmutableMap.<String, Object>builder()
                .put("trailing", 8)
                .build());

        leading = new MaskConcern("mask", ImmutableMap.<String, Object>builder()
                .put("leading", 8)
                .build());

        range = new MaskConcern("mask", ImmutableMap.<String, Object>builder()
                .put("from", 6)
                .put("to", 14)
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

    @Test
    public void should_mask_property_leading() {
        final Record record = builder()
                .path("id_number").value("510108198801011212")
                .get();


        leading.effect(record, "id_number", Record.Property.Point.DELIVERY);

        final Record.Property idNumber = record.property("id_number").get();
        assertThat(idNumber.value(), is("********8801011212"));
    }

    @Test
    public void should_mask_property_range() {
        final Record record = builder()
                .path("id_number").value("510108198801011212")
                .get();


        range.effect(record, "id_number", Record.Property.Point.DELIVERY);

        final Record.Property idNumber = record.property("id_number").get();
        assertThat(idNumber.value(), is("510108********1212"));

    }
}
