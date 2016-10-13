package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.core.Record;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.github.ucluster.common.SimpleRecord.builder;
import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DefaultPropertyDefinitionTest {

    private DefaultRecordDefinition.PropertyDefinition<Record> definition;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefaultPropertyDefinition<>("username",
                asList(
                        new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                .put("pattern", "\\w{6,12}")
                                .build()),
                        new RequiredConcern("required", true)
                )
        );
    }

    @Test
    public void should_get_definition() {
        final Map<String, Object> json = definition.definition();

        final Map<String, Object> formatValidatorConfiguration = (Map<String, Object>) json.get("format");
        assertThat(formatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_validate_property() {
        final Record record = builder()
                .path("username").value("kiwiwin")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_property_if_one_of_the_validator_failed() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("required")
        );

        final Record record = builder().get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_property() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record record = builder()
                .path("username").value("kiwi")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }
}