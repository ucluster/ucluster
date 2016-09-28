package com.github.ucluster.common.definition;

import com.github.ucluster.common.definition.validator.FormatValidator;
import com.github.ucluster.common.definition.validator.RequiredValidator;
import com.github.ucluster.common.definition.validator.UniquenessValidator;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DSLCompilerTest {

    private UserRepository users;

    private UserDefinition definition;

    @Before
    public void setUp() throws Exception {
        Injector injector = getInjector();

        definition = DSLCompiler.load(injector, read("dsl.js"));
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

    @Test
    public void should_get_definition() {
        final UserDefinition.PropertyDefinition passwordDefinition = definition.property("password");

        assertThat(passwordDefinition.definition().get("password"), is(true));
    }

    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        users = mock(UserRepository.class);

        when(users.find(any())).thenReturn(Optional.empty());

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(UserRepository.class).toInstance(users);

                        registerValidator("property.format.validator", FormatValidator.class);
                        registerValidator("property.required.validator", RequiredValidator.class);
                        registerValidator("property.uniqueness.validator", UniquenessValidator.class);
                    }

                    private void registerValidator(String key, Class<? extends PropertyValidator> propertyValidatorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named(key)).toInstance(propertyValidatorClass);
                    }
                }}));
    }

    private String read(String classpath) {
        try {
            return Resources.toString(Resources.getResource(classpath), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
