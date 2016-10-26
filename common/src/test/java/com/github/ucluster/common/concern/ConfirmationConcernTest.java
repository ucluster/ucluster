package com.github.ucluster.common.concern;

import com.github.ucluster.confirmation.ConfirmationException;
import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.confirmation.ConfirmationService;
import com.github.ucluster.core.Record;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static com.github.ucluster.common.SimpleRecord.builder;
import static com.github.ucluster.test.framework.matcher.ConcernEffectExceptionMatcher.capture;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfirmationConcernTest {

    private ConfirmationConcern confirmationConcern;
    private ConfirmationService emailConfirmationService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        confirmationConcern = new ConfirmationConcern("confirm", "email");

        ConfirmationRegistry registry = mock(ConfirmationRegistry.class);
        emailConfirmationService = mock(ConfirmationService.class);

        Injector injector = Guice.createInjector(getAbstractModule(registry));

        injector.injectMembers(confirmationConcern);

        when(registry.find("email")).thenReturn(Optional.of(emailConfirmationService));
    }

    @Test
    public void should_confirm_ok_when_confirmation_service_confirmed_ok() throws Exception {
        Record record = builder()
                .withMetadata("token", "3321")
                .path("email").value("kiwiwin@qq.com")
                .get();

        emailConfirmationService.confirm("kiwiwin@qq.com", "3321");

        confirmationConcern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_confirm_fail_when_confirmation_service_confirmed_failed() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .withMetadata("token", "3321")
                .path("email").value("kiwiwin@qq.com")
                .get();

        doThrow(new ConfirmationException()).when(emailConfirmationService).confirm("kiwiwin@qq.com", "3321");

        confirmationConcern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_confirm_fail_when_confirmation_service_property_not_exist() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .withMetadata("token", "3321")
                .get();

        doThrow(new ConfirmationException()).when(emailConfirmationService).confirm("kiwiwin@qq.com", "3321");

        confirmationConcern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    @Test
    public void should_confirm_fail_when_confirmation_service_token_not_exist_in_metadata() {
        capture(thrown).errors(
                (path, type) -> path.equals("email") && type.equals("confirm"));

        Record record = builder()
                .path("email").value("kiwiwin@qq.com")
                .get();

        doThrow(new ConfirmationException()).when(emailConfirmationService).confirm("kiwiwin@qq.com", "3321");

        confirmationConcern.effect(record, "email", Record.Property.Point.BEFORE_CREATE);
    }

    private AbstractModule getAbstractModule(final ConfirmationRegistry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<ConfirmationService>() {
                }).annotatedWith(Names.named("confirmation.email.method")).toInstance(emailConfirmationService);
                bind(new TypeLiteral<ConfirmationRegistry>() {
                }).toInstance(registry);
            }
        };
    }
}