package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.ConcernEffectException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailConcernTest {

    private Record.Property.Concern<User> concern;
    private User user;
    private Record.Property property;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        concern = new EmailConcern<>("email", true);

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_success_validate_valid_email() {
        when(property.path()).thenReturn("email");
        when(property.value()).thenReturn("kiwi.swhite.coder@gmail.com");

        when(user.property(eq("email"))).thenReturn(Optional.of(property));

        concern.effect(user, "email");
    }

    @Test
    public void should_failed_validate_invalid_email() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                return !exception.getEffectResult().valid();
            }
        });

        when(property.path()).thenReturn("email");
        when(property.value()).thenReturn("invalid.email");

        when(user.property(eq("email"))).thenReturn(Optional.of(property));

        concern.effect(user, "email");
    }
}
