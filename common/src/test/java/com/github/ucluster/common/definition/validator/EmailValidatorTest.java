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

public class EmailValidatorTest {

    private PropertyValidator validator;
    private User user;
    private Record.Property property;

    @Before
    public void setUp() throws Exception {
        validator = new EmailValidator("email", true);

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_success_validate_valid_email() {
        when(property.path()).thenReturn("email");
        when(property.value()).thenReturn("kiwi.swhite.coder@gmail.com");

        when(user.property(eq("email"))).thenReturn(Optional.of(property));


        final ValidationResult result = validator.validate(user, "email");
        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_validate_invalid_email() {
        when(property.path()).thenReturn("email");
        when(property.value()).thenReturn("invalid.email");

        when(user.property(eq("email"))).thenReturn(Optional.of(property));


        final ValidationResult result = validator.validate(user, "email");
        assertThat(result.valid(), is(false));
    }
}
