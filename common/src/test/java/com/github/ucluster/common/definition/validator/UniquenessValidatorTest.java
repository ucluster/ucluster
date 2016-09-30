package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.ActiveRecord;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UniquenessValidatorTest {
    private PropertyValidator uniquenessValidator;
    private PropertyValidator nonUniquenessValidator;
    private Repository<User> users;
    private User user;
    private ActiveRecord.Property property;

    @Before
    public void setUp() throws Exception {
        uniquenessValidator = new UniquenessValidator("uniqueness", true);
        nonUniquenessValidator = new UniquenessValidator("uniqueness", false);

        Injector injector = getInjector();
        injector.injectMembers(uniquenessValidator);

        user = mock(User.class);
        property = mock(ActiveRecord.Property.class);
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_required() {
        when(users.find(argThat(new ArgumentMatcher<ActiveRecord.Property>() {
            @Override
            public boolean matches(Object argument) {
                final ActiveRecord.Property property = (ActiveRecord.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("newusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = uniquenessValidator.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_when_not_unique_and_uniqueness_is_required() {
        when(users.find(argThat(new ArgumentMatcher<ActiveRecord.Property>() {
            @Override
            public boolean matches(Object argument) {
                final ActiveRecord.Property property = (ActiveRecord.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(user));

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("existusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = uniquenessValidator.validate(user, "username");

        assertThat(result.valid(), is(false));
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<ActiveRecord.Property>() {
            @Override
            public boolean matches(Object argument) {
                final ActiveRecord.Property property = (ActiveRecord.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("newusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = nonUniquenessValidator.validate(user, "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_success_when_not_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<ActiveRecord.Property>() {
            @Override
            public boolean matches(Object argument) {
                final ActiveRecord.Property property = (ActiveRecord.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(user));

        when(property.path()).thenReturn("username");
        when(property.value()).thenReturn("existusername");

        when(user.property(eq("username"))).thenReturn(Optional.of(property));

        final ValidationResult result = nonUniquenessValidator.validate(user, "username");

        assertThat(result.valid(), is(true));
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
                        bind(new TypeLiteral<Repository<User>>() {
                        }).toInstance(users);
                    }
                }}));
    }
}