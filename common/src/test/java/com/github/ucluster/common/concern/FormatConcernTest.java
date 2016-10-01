package com.github.ucluster.common.concern;

import com.github.ucluster.common.RecordMock;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FormatConcernTest {

    private Record.Property.Concern concern;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        concern = new FormatConcern("format", ImmutableMap.<String, Object>builder()
                .put("pattern", "\\w{6,12}")
                .build());
    }

    @Test
    public void should_get_configuration() {
        final Map<String, Object> configuration = (Map<String, Object>) concern.configuration();

        assertThat(configuration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_if_property_is_null() {
        final Record record = RecordMock.builder()
                .path("username").none()
                .get();

        concern.effect(record, "username");
    }

    @Test
    public void should_success_validate_against_format() {
        final Record record = RecordMock.builder()
                .path("username").value("kiwiwin")
                .get();

        concern.effect(record, "username");
    }

    @Test
    public void should_failed_to_validate_against_format() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                final EffectResult result = exception.getEffectResult();
                if (result.valid() || result.errors().size() != 1) {
                    return false;
                }
                ;

                final EffectResult.Failure failure = result.errors().get(0);

                return failure.getPropertyPath().equals("username") && failure.getType().equals("format");
            }
        });

        final Record record = RecordMock.builder()
                .path("username").value("kiwi")
                .get();

        concern.effect(record, "username");
    }
}
