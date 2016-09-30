package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.ConcernEffectException;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UniquenessConcernTest {
    private Record.Property.Concern uniqueness;
    private Record.Property.Concern nonUniqueness;
    private Repository<User> users;
    private User user;
    private Record.Property property;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        uniqueness = new UniquenessConcern("uniqueness", true);
        nonUniqueness = new UniquenessConcern("uniqueness", false);

        Injector injector = getInjector();
        injector.injectMembers(uniqueness);

        user = mock(User.class);
        property = mock(Record.Property.class);
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_required() {
        when(users.find(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("newusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        uniqueness.effect(user, "username");
    }

    @Test
    public void should_failed_when_not_unique_and_uniqueness_is_required() {
        thrown.expect(ConcernEffectException.class);

        when(users.find(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(user));

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("existusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        uniqueness.effect(user, "username");
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("newusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        nonUniqueness.effect(user, "username");
    }

    @Test
    public void should_success_when_not_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(user));

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("existusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        nonUniqueness.effect(user, "username");
    }

    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        users = mock(Repository.class);

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(new TypeLiteral<Repository<? extends Record>>() {
                        }).toInstance(users);
                    }
                }}));
    }
}