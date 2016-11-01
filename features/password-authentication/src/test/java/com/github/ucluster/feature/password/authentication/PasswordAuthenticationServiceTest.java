package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.mongo.MongoProperty;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PasswordAuthenticationServiceTest {

    private PasswordAuthenticationService authenticationService;
    private Repository<User> users;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        //TODO: setup by loading feature.js
        final Injector injector = initInjector();

        authenticationService = new PasswordAuthenticationService(ImmutableMap.<String, Object>builder()
                .put("identities", asList("username"))
                .put("password", "password")
                .build()
        );

        injector.injectMembers(authenticationService);
    }

    private Injector initInjector() {
        users = mock(Repository.class);

        return createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<Repository<User>>() {
                }).toInstance(users);
            }
        });
    }

    @Test
    public void should_success_authenticate_user() {
        //TODO: simplify mock user with user repository
        final User user = mock(User.class);
        when(user.property("username")).thenReturn(Optional.of(new MongoProperty<>("username", "kiwiwin")));
        when(user.property("password")).thenReturn(Optional.of(new MongoProperty<>("password", Encryption.BCRYPT.encrypt("password"))));

        when(users.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("kiwiwin");
            }
        }))).thenReturn(Optional.of(user));

        final User authenticatedUser = authenticationService.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("password", "password")
                .build()
        );

        assertThat(authenticatedUser.property("username").get().value(), is("kiwiwin"));
    }

    @Test
    public void should_failed_authenticate_user_when_no_identity_matched() {
        thrown.expect(AuthenticationException.class);

        when(users.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("notexist");
            }
        }))).thenReturn(Optional.empty());

        authenticationService.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "notexist")
                .put("password", "password")
                .build()
        );
    }

    @Test
    public void should_failed_authenticate_user_when_password_not_matched() {
        thrown.expect(AuthenticationException.class);

        final User user = mock(User.class);
        when(user.property("username")).thenReturn(Optional.of(new MongoProperty<>("username", "kiwiwin")));
        when(user.property("password")).thenReturn(Optional.of(new MongoProperty<>("password", Encryption.BCRYPT.encrypt("password"))));

        when(users.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("kiwiwin");
            }
        }))).thenReturn(Optional.of(user));

        authenticationService.authenticate(ImmutableMap.<String, Object>builder()
                .put("username", "kiwiwin")
                .put("password", "wrongpassword")
                .build()
        );
    }
}
