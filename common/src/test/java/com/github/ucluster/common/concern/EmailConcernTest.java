package com.github.ucluster.common.concern;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.exception.ConcernEffectException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.ucluster.common.RecordMock.builder;

public class EmailConcernTest {

    private Record.Property.Concern concern;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        concern = new EmailConcern("email", true);
    }

    @Test
    public void should_success_validate_valid_email() {
        final Record record = builder()
                .path("email").value("kiwi.swhite.coder@gmail.com")
                .get();

        concern.effect(record, "email");
    }

    @Test
    public void should_failed_validate_invalid_email() {
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

        final Record record = builder()
                .path("email").value("invalid.email")
                .get();

        concern.effect(record, "email");
    }
}
