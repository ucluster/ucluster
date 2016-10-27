package com.github.ucluster.common.concern;

import com.github.ucluster.confirmation.ConfirmationException;
import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.confirmation.ConfirmationService;
import com.github.ucluster.core.Record;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static com.github.ucluster.common.SimpleRecord.builder;
import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfirmationConcernTest {

    private ConfirmationConcern concern;
    private ConfirmationService confirmationService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        confirmationService = mock(ConfirmationService.class);

        final ConfirmationRegistry registry = mock(ConfirmationRegistry.class);
        when(registry.find("email")).thenReturn(Optional.of(confirmationService));
        when(registry.find("non_exist_method")).thenReturn(Optional.empty());

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ConfirmationRegistry.class).toInstance(registry);
            }
        });

        concern = new ConfirmationConcern("confirm", "email");
        injector.injectMembers(concern);
    }

    @Test
    public void should_confirm_success_when_confirmation_service_confirmed_success() {
        Record record = builder()
                .metadata("token", "3321")
                .path("email").value("kiwi.swhite.coder@gmail.com")
                .get();

        concern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);

        verify(confirmationService).confirm("kiwi.swhite.coder@gmail.com", "3321");
    }

    @Test
    public void should_confirm_failed_when_confirmation_service_confirmed_failed() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .metadata("token", "3321")
                .path("email").value("kiwi.swhite.coder@gmail.com")
                .get();

        doThrow(new ConfirmationException()).when(confirmationService).confirm("kiwi.swhite.coder@gmail.com", "3321");

        concern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_confirm_failed_when_confirmation_service_property_not_exist() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .metadata("token", "3321")
                .get();

        concern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_confirm_failed_when_confirmation_service_token_not_exist_in_metadata() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .path("email").value("kiwi.swhite.coder@gmail.com")
                .get();

        doThrow(new ConfirmationException()).when(confirmationService).confirm("kiwi.swhite.coder@gmail.com", "3321");

        concern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }
}