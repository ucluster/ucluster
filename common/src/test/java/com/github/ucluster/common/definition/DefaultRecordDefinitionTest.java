package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.common.ConcernEffectExceptionMatcher.capture;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRecordDefinitionTest {

    private DefaultRecordDefinition definition;
    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefaultRecordDefinition<>(asList(
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
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

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
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format"),
                (path, type) -> path.equals("nickname") && type.equals("format")
        );

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
