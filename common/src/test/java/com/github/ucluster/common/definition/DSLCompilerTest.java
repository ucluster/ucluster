package com.github.ucluster.common.definition;

import com.github.ucluster.common.definition.processor.ImmutableProcessor;
import com.github.ucluster.common.definition.processor.PasswordProcessor;
import com.github.ucluster.common.definition.validator.EmailValidator;
import com.github.ucluster.common.definition.validator.FormatValidator;
import com.github.ucluster.common.definition.validator.IdentityValidator;
import com.github.ucluster.common.definition.validator.RequiredValidator;
import com.github.ucluster.common.definition.validator.UniquenessValidator;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.base.Charsets;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DSLCompilerTest {

    private UserRepository users;

    private UserDefinition definition;
    private User user;

    @Before
    public void setUp() throws Exception {
        Injector injector = getInjector();

        definition = DSLCompiler.load(injector, read("dsl.js"));

        user = mock(User.class);
    }

    @Test
    public void should_verify_by_dsl() {
        final User.Property usernameProperty = mock(User.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwiwin");

        final User.Property passwordProperty = mock(User.Property.class);
        when(passwordProperty.path()).thenReturn("password");
        when(passwordProperty.value()).thenReturn("password");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));

        final ValidationResult result = definition.validate(user);

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_verify_by_dsl() {
        final User.Property usernameProperty = mock(User.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        final User.Property passwordProperty = mock(User.Property.class);
        when(passwordProperty.path()).thenReturn("password");
        when(passwordProperty.value()).thenReturn("password");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));

        final ValidationResult result = definition.validate(user);

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

                        registerValidator("format", FormatValidator.class);
                        registerValidator("email", EmailValidator.class);
                        registerValidator("required", RequiredValidator.class);
                        registerValidator("uniqueness", UniquenessValidator.class);
                        registerValidator("identity", IdentityValidator.class);

                        registerProcessor("password", PasswordProcessor.class);
                        registerProcessor("immutable", ImmutableProcessor.class);
                    }

                    private void registerValidator(String type, Class<? extends PropertyValidator> propertyValidatorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named("property." + type + ".validator")).toInstance(propertyValidatorClass);
                    }

                    private void registerProcessor(String type, Class<? extends PropertyProcessor> propertyProcessorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named("property." + type + ".processor")).toInstance(propertyProcessorClass);
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
