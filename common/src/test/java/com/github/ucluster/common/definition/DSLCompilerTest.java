package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.PasswordConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.UniquenessConcern;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    private Repository<User> users;

    private DefaultUserDefinition definition;
    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Injector injector = getInjector();

        definition = DSLCompiler.load(injector, read("dsl.js"));

        user = mock(User.class);
    }

    @Test
    public void should_verify_by_dsl() {
        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwiwin");

        final Record.Property passwordProperty = mock(Record.Property.class);
        when(passwordProperty.path()).thenReturn("password");
        when(passwordProperty.value()).thenReturn("password");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));

        definition.effect(Record.Property.Concern.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_verify_by_dsl() {
        thrown.expect(ConcernEffectException.class);
        thrown.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expects RecordValidationException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                return !exception.getEffectResult().valid();
            }
        });

        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        final Record.Property passwordProperty = mock(Record.Property.class);
        when(passwordProperty.path()).thenReturn("password");
        when(passwordProperty.value()).thenReturn("password");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));
        when(user.properties()).thenReturn(asList(usernameProperty, passwordProperty));

        definition.effect(Record.Property.Concern.Point.VALIDATE, user);
    }

    @Test
    public void should_get_definition() {
        final DefaultUserDefinition.PropertyDefinition passwordDefinition = definition.property("password");

        assertThat(passwordDefinition.definition().get("password"), is(true));
    }

    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        users = mock(Repository.class);

        when(users.find(any())).thenReturn(Optional.empty());

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(new TypeLiteral<Repository<User>>() {
                        }).toInstance(users);

                        registerConcern("format", FormatConcern.class);
                        registerConcern("email", EmailConcern.class);
                        registerConcern("required", RequiredConcern.class);
                        registerConcern("uniqueness", UniquenessConcern.class);
                        registerConcern("identity", IdentityConcern.class);

                        registerConcern("password", PasswordConcern.class);
                        registerConcern("immutable", ImmutableConcern.class);
                    }

                    private void registerConcern(String type, Class<? extends Record.Property.Concern<User>> concernClass) {
                        bind(new TypeLiteral<Class<? extends Record.Property.Concern<User>>>() {
                        }).annotatedWith(Names.named("property." + type + ".concern")).toInstance(concernClass);
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
