package com.github.ucluster.common.concern;

import com.github.ucluster.common.SimpleRecord;
import com.github.ucluster.core.Record;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.ucluster.common.ValidationMatcher.capture;

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
        final Record record = SimpleRecord.builder()
                .path("username").value("kiwiwin")
                .get();

        required.effect(record, "username");
    }

    @Test
    public void should_failed_required_but_value_absence() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("required")
        );

        final Record record = SimpleRecord.builder()
                .path("username").none()
                .get();

        required.effect(record, "username");
    }

    @Test
    public void should_success_optional_when_value_presence() {
        final Record record = SimpleRecord.builder()
                .path("username").value("kiwiwin")
                .get();

        optional.effect(record, "username");
    }

    @Test
    public void should_success_optional_but_value_absence() {
        final Record record = SimpleRecord.builder()
                .path("username").none()
                .get();

        optional.effect(record, "username");
    }
}
