package com.github.ucluster.mongo.validator;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UserDefinitionTest {

    private DefaultUserDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = new DefaultUserDefinition(
                ImmutableMap.<String, UserDefinition.PropertyDefinition>builder()
                        .put("username", new DefaultPropertyDefinition(
                                ImmutableMap.<String, PropertyValidator>builder()
                                        .put("format", new FormatPropertyValidator("username", ImmutableMap.<String, Object>builder()
                                                .put("pattern", "\\w{6,12}")
                                                .build()))
                                        .build()))
                        .put("nickname", new DefaultPropertyDefinition(
                                ImmutableMap.<String, PropertyValidator>builder()
                                        .put("format", new FormatPropertyValidator("nickname", ImmutableMap.<String, Object>builder()
                                                .put("pattern", "\\w{6,12}")
                                                .build()))
                                        .build()))
                        .build()
        );
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
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("nickname", "kiwiwin")
                .build());

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_validate_user_has_exactly_one_error() {
        final ValidationResult result = definition.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("nickname", "kiwiwin")
                        .build());

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(1));
        final ValidationResult.ValidateFailure failure = result.errors().get(0);
        assertThat(failure.getPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }

    @Test
    public void should_failed_validate_user_has_more_than_one_error() {
        final ValidationResult result = definition.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "kiwi")
                        .put("nickname", "kiwi")
                        .build());

        assertThat(result.valid(), is(false));

        assertThat(result.errors().size(), is(2));
        final ValidationResult.ValidateFailure usernameFailure = result.errors().get(0);
        assertThat(usernameFailure.getPath(), is("username"));
        assertThat(usernameFailure.getType(), is("format"));

        final ValidationResult.ValidateFailure nicknameFailure = result.errors().get(1);
        assertThat(nicknameFailure.getPath(), is("nickname"));
        assertThat(nicknameFailure.getType(), is("format"));
    }
}
