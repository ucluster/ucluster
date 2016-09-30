package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.RequiredConcern;
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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPropertyDefinitionTest {

    private DefaultUserDefinition.PropertyDefinition<User> definition;
    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefaultPropertyDefinition("username",
                asList(
                        new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                .put("pattern", "\\w{6,12}")
                                .build()),
                        new RequiredConcern("required", true)
                )
        );

        user = mock(User.class);
    }

    @Test
    public void should_get_definition() {
        final Map<String, Object> json = definition.definition();

        final Map<String, Object> formatValidatorConfiguration = (Map<String, Object>) json.get("format");
        assertThat(formatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_validate_property() {
        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));

        definition.effect(Record.Property.Concern.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_validate_property_if_one_of_the_validator_failed() {
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

                final EffectResult.ValidateFailure failure = result.errors().get(0);

                return failure.getPropertyPath().equals("username") && failure.getType().equals("required");
            }
        });

        when(user.property(eq("username"))).thenReturn(Optional.empty());

        definition.effect(Record.Property.Concern.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_validate_property() {
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

                final EffectResult.ValidateFailure failure = result.errors().get(0);

                return failure.getPropertyPath().equals("username") && failure.getType().equals("format");
            }
        });

        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));

        definition.effect(Record.Property.Concern.Point.VALIDATE, user);
    }
}
