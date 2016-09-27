package com.github.ucluster.mongo.dsl;

import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DSLTest {

    private UserDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = DSL.load(read("dsl.js"));
    }

    @Test
    public void should_get_definition() {

    }

    @Test
    public void should_verify_by_dsl() {
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("password", "password")
                .build()
        );

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_verify_by_dsl() {
        final ValidationResult result = definition.validate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwi")
                .put("password", "password")
                .build()
        );

        assertThat(result.valid(), is(false));

        final List<ValidationResult.ValidateFailure> errors = result.errors();
        assertThat(errors.size(), is(1));

        final ValidationResult.ValidateFailure failure = errors.get(0);
        assertThat(failure.getPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }

    private static String read(String name) throws IOException {
        return Resources.toString(Resources.getResource(name), Charsets.UTF_8);
    }
}
