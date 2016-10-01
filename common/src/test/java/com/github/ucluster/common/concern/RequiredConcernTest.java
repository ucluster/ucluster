package com.github.ucluster.common.concern;

import com.github.ucluster.common.RecordMock;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.exception.ConcernEffectException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    public void should_success_required_when_value_presence() {
        final Record record = RecordMock.builder()
                .path("username").value("kiwiwin")
                .get();

        required.effect(record, "username");
    }

    @Test
    public void should_failed_required_but_value_absence() {
        thrown.expect(ConcernEffectException.class);

        final Record record = RecordMock.builder()
                .path("username").none()
                .get();

        required.effect(record, "username");
    }

    @Test
    public void should_success_optional_when_value_presence() {
        final Record record = RecordMock.builder()
                .path("username").value("kiwiwin")
                .get();

        optional.effect(record, "username");
    }

    @Test
    public void should_success_optional_but_value_absence() {
        final Record record = RecordMock.builder()
                .path("username").none()
                .get();

        optional.effect(record, "username");
    }
}
