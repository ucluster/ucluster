package com.github.ucluster.common.definition.validator;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.ValidationResult;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UniquenessValidatorTest {
    private PropertyValidator uniquenessValidator;
    private PropertyValidator nonUniquenessValidator;
    private UserRepository users;

    @Before
    public void setUp() throws Exception {
        uniquenessValidator = new UniquenessValidator("uniqueness", true);
        nonUniquenessValidator = new UniquenessValidator("uniqueness", false);

        Injector injector = getInjector();
        injector.injectMembers(uniquenessValidator);
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_required() {
        when(users.find(argThat(new ArgumentMatcher<User.Property>() {
            @Override
            public boolean matches(Object argument) {
                final User.Property property = (User.Property) argument;
                return property.key().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        final ValidationResult result = uniquenessValidator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "newusername")
                        .build(),
                "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_failed_when_not_unique_and_uniqueness_is_required() {
        when(users.find(argThat(new ArgumentMatcher<User.Property>() {
            @Override
            public boolean matches(Object argument) {
                final User.Property property = (User.Property) argument;
                return property.key().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(mock(User.class)));

        final ValidationResult result = uniquenessValidator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "existusername")
                        .build(),
                "username");

        assertThat(result.valid(), is(false));
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<User.Property>() {
            @Override
            public boolean matches(Object argument) {
                final User.Property property = (User.Property) argument;
                return property.key().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        final ValidationResult result = nonUniquenessValidator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "newusername")
                        .build(),
                "username");

        assertThat(result.valid(), is(true));
    }

    @Test
    public void should_success_when_not_unique_and_uniqueness_is_not_required() {
        when(users.find(argThat(new ArgumentMatcher<User.Property>() {
            @Override
            public boolean matches(Object argument) {
                final User.Property property = (User.Property) argument;
                return property.key().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(mock(User.class)));

        final ValidationResult result = nonUniquenessValidator.validate(
                ImmutableMap.<String, Object>builder()
                        .put("username", "existusername")
                        .build(),
                "username");

        assertThat(result.valid(), is(true));
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