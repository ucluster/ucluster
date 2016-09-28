package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormatValidatorTest {

    private PropertyValidator validator;
    private User user;
    private User.Property property;

    @Before
    public void setUp() throws Exception {
        validator = new FormatValidator("format", ImmutableMap.<String, Object>builder()
                .put("pattern", "\\w{6,12}")
                .build());

        user = mock(User.class);
        property = mock(User.Property.class);
    }

    @Test
    public void should_get_configuration() {
        final Map<String, Object> configuration = (Map<String, Object>) validator.configuration();

        assertThat(configuration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_if_property_is_null() {
        when(user.property(eq("username"))).thenReturn(Optional.empty());

        final ValidationResult result = validator.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_success_validate_against_format() {
        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = validator.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_to_validate_against_format() {
        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("kiwi");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = validator.validate(user, "username");

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(1));
        final ValidationResult.ValidateFailure failure = result.errors().get(0);
        assertThat(failure.getPropertyPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }
}
