package com.github.ucluster.common;

import com.github.ucluster.core.exception.ConcernEffectException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

import java.util.function.Function;

public class ValidationMatcher {
    private final ExpectedException expected;

    public ValidationMatcher(ExpectedException expected) {
        this.expected = expected;
    }

    public void is(Function<ConcernEffectException, Boolean> function) {
        expected.expect(ConcernEffectException.class);
        expected.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("should found ConcernEffectException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                return function.apply(exception);
            }
        });
    }

    public static ValidationMatcher of(ExpectedException expected) {
        return new ValidationMatcher(expected);
    }
}
