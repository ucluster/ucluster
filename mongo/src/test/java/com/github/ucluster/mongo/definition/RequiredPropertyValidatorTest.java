package com.github.ucluster.mongo.definition;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RequiredPropertyValidatorTest {

    private PropertyValidator required;
    private PropertyValidator optional;

    @Before
    public void setUp() throws Exception {
        required = new RequiredPropertyValidator(true);
        optional = new RequiredPropertyValidator(false);
    }

    @Test
    public void should_success_required_when_value_presence() {
        final ValidationResult result = required.validate(ImmutableMap.<String, Object>builder().put("username", "kiwiwin").build(), "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_required_but_value_absence() {
        final ValidationResult result = required.validate(new HashMap<>(), "username");

        assertThat(result.valid(), is(false));
    }

    @Test
    public void should_success_optional_when_value_presence() {
        final ValidationResult result = optional.validate(ImmutableMap.<String, Object>builder().put("username", "kiwiwin").build(), "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_success_optional_but_value_absence() {
        final ValidationResult result = optional.validate(new HashMap<>(), "username");

        assertThat(result.valid(), is(true));
    }
}
