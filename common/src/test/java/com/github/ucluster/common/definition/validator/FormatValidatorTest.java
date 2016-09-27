package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FormatValidatorTest {

    private PropertyValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new FormatValidator(
                ImmutableMap.<String, Object>builder()
                        .put("pattern", "\\w{6,12}")
                        .build());
    }

    @Test
    public void should_get_configuration() {
        final Map<String, Object> configuration = (Map<String, Object>) validator.configuration();

        assertThat(configuration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_if_path_value_is_null() {
        final ValidationResult result = validator.validate(new HashMap<>(), "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_validate_against_format() {
        final ValidationResult result = validator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "kiwiwin").build(), "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_to_validate_against_format() {
        final ValidationResult result = validator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi").build(), "username");

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(1));
        final ValidationResult.ValidateFailure failure = result.errors().get(0);
        assertThat(failure.getPropertyPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }
}
