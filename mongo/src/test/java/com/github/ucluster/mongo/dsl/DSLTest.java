package com.github.ucluster.mongo.dsl;

import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DSLTest {

    private Injector injector;
    private UserRepository users;

    private DSL dsl;

    private UserDefinition definition;

    @Before
    public void setUp() throws Exception {
        injector = getInjector();

        dsl = new DSL();
        injector.injectMembers(dsl);

        definition = dsl.load(read("dsl.js"));
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
        assertThat(failure.getPropertyPath(), is("username"));
        assertThat(failure.getType(), is("format"));
    }

    private static String read(String name) throws IOException {
        return Resources.toString(Resources.getResource(name), Charsets.UTF_8);
    }


    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        users = mock(UserRepository.class);

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(UserRepository.class).toInstance(users);
                    }
                }}));
    }
}
