package com.github.ucluster.common.concern;

import com.github.ucluster.common.SimpleRecord;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static com.google.inject.Guice.createInjector;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UniquenessConcernTest {
    private Record.Property.Concern uniqueness;
    private Record.Property.Concern nonUniqueness;
    private Repository<Record> records;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        uniqueness = new UniquenessConcern("uniqueness", true);
        nonUniqueness = new UniquenessConcern("uniqueness", false);

        Injector injector = getInjector();
        injector.injectMembers(uniqueness);
    }


    @Test
    public void should_uniqueness_care_about_before_create_and_before_update() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, true)
                .put(Record.Property.Point.BEFORE_UPDATE, true)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(uniqueness.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_non_uniqueness_care_about_nothing() {
        final Map<Record.Property.Point, Boolean> expected = ImmutableMap.<Record.Property.Point, Boolean>builder()
                .put(Record.Property.Point.VALIDATE, false)
                .put(Record.Property.Point.BEFORE_CREATE, false)
                .put(Record.Property.Point.BEFORE_UPDATE, false)
                .build();

        expected.entrySet().stream()
                .forEach(entry ->
                        assertThat(nonUniqueness.isAbout(entry.getKey()), is(entry.getValue()))
                );
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_required() {
        final Record record = SimpleRecord.builder()
                .path("username").value("newusername")
                .get();

        when(records.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        uniqueness.effect(record, "username", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_failed_when_not_unique_and_uniqueness_is_required() {
        capture(thrown).errors(
                (path, type) -> path.equals("username") && type.equals("uniqueness")
        );

        final Record record = SimpleRecord.builder()
                .path("username").value("existusername")
                .get();

        when(records.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(record));

        uniqueness.effect(record, "username", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_success_when_unique_and_uniqueness_is_not_required() {
        final Record record = SimpleRecord.builder()
                .path("username").value("newusername")
                .get();

        when(records.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("newusername");
            }
        }))).thenReturn(Optional.empty());

        nonUniqueness.effect(record, "username", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_success_when_not_unique_and_uniqueness_is_not_required() {
        final Record record = SimpleRecord.builder()
                .path("username").value("existusername")
                .get();

        when(records.findBy(argThat(new ArgumentMatcher<Record.Property>() {
            @Override
            public boolean matches(Object argument) {
                final Record.Property property = (Record.Property) argument;
                return property.path().equals("username") && property.value().equals("existusername");
            }
        }))).thenReturn(Optional.of(record));

        nonUniqueness.effect(record, "username", Record.Property.Point.BEFORE_CREATE);
    }

    private Injector getInjector() {
        return createInjector(getAbstractModules());
    }

    private List<AbstractModule> getAbstractModules() {
        records = mock(Repository.class);

        return new ArrayList<>(asList(new AbstractModule[]{
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(new TypeLiteral<Repository<? extends Record>>() {
                        }).toInstance(records);
                    }
                }}));
    }
}