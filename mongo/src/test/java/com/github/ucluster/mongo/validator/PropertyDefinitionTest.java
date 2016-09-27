package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PropertyDefinitionTest {

    private UserDefinition.PropertyDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = new DefaultPropertyDefinition("username",
                ImmutableMap.<String, PropertyValidator>builder()
                        .put("format",
                                new FormatPropertyValidator(ImmutableMap.<String, Object>builder()
                                        .put("pattern", "\\w{6,12}")
                                        .build()))
                        .put("required", new RequiredPropertyValidator(true))
                        .build()
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
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin").build());

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_validate_property_if_one_of_the_validator_failed() {
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder().build());

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(1));
        final ValidationResult.ValidateFailure failure = result.errors().get(0);
        assertThat(failure.getPropertyPath(), is("username"));
        assertThat(failure.getType(), is("required"));
    }

    @Test
    public void should_failed_validate_property() {
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwi").build());

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(1));
        final ValidationResult.ValidateFailure failure = result.errors().get(0);
        assertThat(failure.getPropertyPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }
}
