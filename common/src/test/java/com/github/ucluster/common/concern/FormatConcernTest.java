package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.RecordValidationException;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormatConcernTest {

    private Record.Property.Concern<User> concern;
    private User user;
    private Record.Property property;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        concern = new FormatConcern("format", ImmutableMap.<String, Object>builder()
                .put("pattern", "\\w{6,12}")
                .build());

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_get_configuration() {
        final Map<String, Object> configuration = (Map<String, Object>) concern.configuration();

        assertThat(configuration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_if_property_is_null() {
        when(user.property(eq("username"))).thenReturn(Optional.empty());

        concern.effect(user, "username");
    }

    @Test
    public void should_success_validate_against_format() {
        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        concern.effect(user, "username");
    }

    @Test
    public void should_failed_to_validate_against_format() {
        thrown.expect(RecordValidationException.class);
        thrown.expect(new TypeSafeMatcher<RecordValidationException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(RecordValidationException exception) {
                final EffectResult result = exception.getEffectResult();
                if (result.valid() || result.errors().size() != 1) {
                    return false;
                }
                ;

                final EffectResult.Failure failure = result.errors().get(0);

                return failure.getPropertyPath().equals("username") && failure.getType().equals("format");
            }
        });

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("kiwi");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        concern.effect(user, "username");
    }
}
