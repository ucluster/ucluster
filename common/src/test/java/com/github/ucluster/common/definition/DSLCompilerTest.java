package com.github.ucluster.common.definition;

import com.github.ucluster.common.concern.ConfirmationConcern;
import com.github.ucluster.common.concern.CredentialConcern;
import com.github.ucluster.common.concern.EmailConcern;
import com.github.ucluster.common.concern.FormatConcern;
import com.github.ucluster.common.concern.IdentityConcern;
import com.github.ucluster.common.concern.ImmutableConcern;
import com.github.ucluster.common.concern.RequiredConcern;
import com.github.ucluster.common.concern.UniquenessConcern;
import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.confirmation.ConfirmationService;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
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

    private DefaultRecordDefinition<User> definition;
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

        final Record.Property emailProperty = mock(Record.Property.class);
        when(emailProperty.path()).thenReturn("email");
        when(emailProperty.value()).thenReturn("kiwi.swhite.coder@gmail.com");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));
        when(user.property(eq("email"))).thenReturn(Optional.of(emailProperty));

        definition.effect(Record.Property.Point.VALIDATE, user);
    }

    @Test
    public void should_failed_verify_by_dsl() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("format")
        );

        final Record.Property usernameProperty = mock(Record.Property.class);
        when(usernameProperty.path()).thenReturn("username");
        when(usernameProperty.value()).thenReturn("kiwi");

        final Record.Property passwordProperty = mock(Record.Property.class);
        when(passwordProperty.path()).thenReturn("password");
        when(passwordProperty.value()).thenReturn("password");

        final Record.Property emailProperty = mock(Record.Property.class);
        when(emailProperty.path()).thenReturn("email");
        when(emailProperty.value()).thenReturn("kiwi.swhite.coder@gmail.com");

        when(user.property(eq("username"))).thenReturn(Optional.of(usernameProperty));
        when(user.property(eq("password"))).thenReturn(Optional.of(passwordProperty));
        when(user.properties()).thenReturn(asList(usernameProperty, passwordProperty));
        when(user.property(eq("email"))).thenReturn(Optional.of(emailProperty));

        definition.effect(Record.Property.Point.VALIDATE, user);
    }

    @Test
    public void should_get_definition() {
        final DefaultRecordDefinition.PropertyDefinition credentialDefinition = definition.property("password");

        assertThat(credentialDefinition.definition().get("credential"), is(true));
    }

    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        users = mock(Repository.class);

        when(users.findBy(any())).thenReturn(Optional.empty());

        ConfirmationRegistry registry = mockRegistry();

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(new TypeLiteral<Repository<? extends Record>>() {
                        }).toInstance(users);
                        bind(new TypeLiteral<ConfirmationRegistry>() {
                        }).toInstance(registry);

                        registerConcern("format").to(new TypeLiteral<FormatConcern>() {
                        });
                        registerConcern("email").to(new TypeLiteral<EmailConcern>() {
                        });
                        registerConcern("required").to(new TypeLiteral<RequiredConcern>() {
                        });
                        registerConcern("uniqueness").to(new TypeLiteral<UniquenessConcern>() {
                        });
                        registerConcern("identity").to(new TypeLiteral<IdentityConcern>() {
                        });
                        registerConcern("credential").to(new TypeLiteral<CredentialConcern>() {
                        });
                        registerConcern("immutable").to(new TypeLiteral<ImmutableConcern>() {
                        });
                        registerConcern("confirm").to(new TypeLiteral<ConfirmationConcern>() {
                        });
                    }

                    private LinkedBindingBuilder<Record.Property.Concern> registerConcern(String type) {
                        return bind(new TypeLiteral<Record.Property.Concern>() {
                        }).annotatedWith(Names.named("property." + type + ".concern"));
                    }
                }}));
    }

    private ConfirmationRegistry mockRegistry() {
        ConfirmationRegistry registry = mock(ConfirmationRegistry.class);
        ConfirmationService confirmService = mock(ConfirmationService.class);
        when(registry.find("email")).thenReturn(Optional.of(confirmService));
        return registry;
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
