package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequiredValidatorTest {

    private PropertyValidator required;
    private PropertyValidator optional;
    private User user;
    private Record.Property property;

    @Before
    public void setUp() throws Exception {
        required = new RequiredValidator("required", true);
        optional = new RequiredValidator("required", false);

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_success_required_when_value_presence() {
        propertyPresent();

        final ValidationResult result = required.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_required_but_value_absence() {
        propertyAbsent();

        final ValidationResult result = required.validate(user, "username");

        assertThat(result.valid(), is(false));
    }

    @Test
    public void should_success_optional_when_value_presence() {
        propertyPresent();

        final ValidationResult result = optional.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_success_optional_but_value_absence() {
        propertyAbsent();

        final ValidationResult result = optional.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    private void propertyPresent() {
        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));
    }

    private void propertyAbsent() {
        when(user.property(eq("username"))).thenReturn(Optional.empty());
    }
}
