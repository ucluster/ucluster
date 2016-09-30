package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RecordValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequiredConcernTest {

    private Record.Property.Concern<User> required;
    private Record.Property.Concern<User> optional;
    private User user;
    private Record.Property property;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        required = new RequiredConcern("required", true);
        optional = new RequiredConcern("required", false);

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_success_required_when_value_presence() {
        propertyPresent();

        required.effect(user, "username");
    }

    @Test
    public void should_failed_required_but_value_absence() {
        thrown.expect(RecordValidationException.class);

        propertyAbsent();

        required.effect(user, "username");
    }

    @Test
    public void should_success_optional_when_value_presence() {
        propertyPresent();

        optional.effect(user, "username");
    }

    @Test
    public void should_success_optional_but_value_absence() {
        propertyAbsent();

        optional.effect(user, "username");
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
