package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
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

public class DefaultUserDefinitionTest {

    private DefaultUserDefinition definition;
    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefaultUserDefinition(asList(
                new DefaultPropertyDefinition("username",
                        asList(new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                .put("pattern", "\\w{6,12}")
                                .build()))),
                new DefaultPropertyDefinition("nickname",
                        asList(new FormatConcern("format", ImmutableMap.<String, Object>builder()
                                .put("pattern", "\\w{6,12}")
                                .build())))
        ));

        user = mock(User.class);

    }

    @Test
    public void should_get_definition() {
        final Map<String, Object> json = definition.definition();

        final Map<String, Object> usernameJson = (Map<String, Object>) json.get("username");
        final Map<String, Object> usernameFormatValidatorConfiguration = (Map<String, Object>) usernameJson.get("format");
        assertThat(usernameFormatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));

        final Map<String, Object> nicknameJson = (Map<String, Object>) json.get("nickname");
        final Map<String, Object> nicknameFormatValidatorConfiguration = (Map<String, Object>) nicknameJson.get("format");
        assertThat(nicknameFormatValidatorConfiguration.get("pattern"), is("\\w{6,12}"));
    }

    @Test
    public void should_success_validate_user() {
        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwiwin");

        final Record.Property nicknameProperty = mock(Record.Property.class);
        when(nicknameProperty.path()).thenReturn("nickname");
        when(nicknameProperty.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("nickname"))).thenReturn(Optional.of(nicknameProperty));
        when(user.properties()).thenReturn(asList(usernameProperty, nicknameProperty));

        definition.effect(Record.Property.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_validate_user_has_exactly_one_error() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                final EffectResult result = exception.getEffectResult();
                if (result.valid() || result.errors().size() != 1) {
                    return false;
                }

                final EffectResult.Failure failure = result.errors().get(0);

                return failure.getPropertyPath().equals("username") && failure.getType().equals("format");
            }
        });

        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        final Record.Property nicknameProperty = mock(Record.Property.class);
        when(nicknameProperty.path()).thenReturn("nickname");
        when(nicknameProperty.value()).thenReturn("kiwiwin");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("nickname"))).thenReturn(Optional.of(nicknameProperty));
        when(user.properties()).thenReturn(asList(usernameProperty, nicknameProperty));

        definition.effect(Record.Property.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_validate_user_has_more_than_one_error() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                final EffectResult result = exception.getEffectResult();
                if (result.valid() || result.errors().size() != 2) {
                    return false;
                }

                final EffectResult.Failure usernameFailure = result.errors().get(0);
                final EffectResult.Failure nicknameFailure = result.errors().get(1);

                return nicknameFailure.getPropertyPath().equals("nickname") && nicknameFailure.getType().equals("format") &&
                        usernameFailure.getPropertyPath().equals("username") && usernameFailure.getType().equals("format");
            }
        });

        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        final Record.Property nicknameProperty = mock(Record.Property.class);
        when(nicknameProperty.path()).thenReturn("nickname");
        when(nicknameProperty.value()).thenReturn("kiwi");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("nickname"))).thenReturn(Optional.of(nicknameProperty));
        when(user.properties()).thenReturn(asList(usernameProperty, nicknameProperty));

        definition.effect(Record.Property.Point.VALIDATE, user);
    }
}
