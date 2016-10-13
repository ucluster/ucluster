package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
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

public class DefaultRecordDefinitionTest {

    private Definition<Record> definition;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefaultRecordDefinition<>(asList(
                new DefaultPropertyDefinition("username",
                        asList(
                                new RequiredConcern("required", true),
                                new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                        .put("pattern", "\\w{6,12}")
                                        .build()))),
                new DefaultPropertyDefinition("nickname",
                        asList(
                                new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                        .put("pattern", "\\w{6,12}")
                                        .build())))
        ));
    }

    @Test
    public void should_get_definition() {
        final Map<String, Object> json = definition.definition();

        final Map<String, Object> usernameJson = (Map<String, Object>) json.get("username");
        final Map<String, Object> usernameFormatValidatorConfiguration = (Map<String, Object>) usernameJson.get("format");
        assertThat(usernameFormatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));

        final Map<String, Object> nicknameJson = (Map<String, Object>) json.get("nickname");
        final Map<String, Object> nicknameFormatValidatorConfiguration = (Map<String, Object>) nicknameJson.get("format");
        assertThat(nicknameFormatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_validate_user() {
        final Record record = builder()
                .path("username").value("kiwiwin")
                .path("nickname").value("kiwiwin")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_record_has_exactly_one_error() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record record = builder()
                .path("username").value("kiwi")
                .path("nickname").value("kiwiwin")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_record_has_more_than_one_error() {
        capture(thrown).errors(
                (path, type) -> path.equals("nickname") && type.equals("format"),
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record record = builder()
                .path("username").value("kiwi")
                .path("nickname").value("kiwi")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_record_has_property_not_defined() {
        capture(thrown).errors(
                (path, type) -> path.equals("fake") && type.equals("undefined")
        );

        final Record record = builder()
                .path("username").value("kiwiwin")
                .path("nickname").value("kiwiwin")
                .path("fake").value("fake")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }

    @Test
    public void should_failed_validate_record_has_property_not_defined_and_other_errors() {
        capture(thrown).errors(
                (path, type) -> path.equals("fake") && type.equals("undefined"),
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record record = builder()
                .path("username").value("kiwi")
                .path("nickname").value("kiwiwin")
                .path("fake").value("fake")
                .get();

        definition.effect(Record.Property.Point.VALIDATE, record);
    }
}
