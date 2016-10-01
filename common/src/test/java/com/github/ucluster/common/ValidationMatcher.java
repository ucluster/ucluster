package com.github.ucluster.common;

import com.github.ucluster.core.definition.EffectResult;
import com.github.ucluster.core.exception.ConcernEffectException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

import java.util.List;

public class ValidationMatcher {
    private final ExpectedException expected;

    private ValidationMatcher(ExpectedException expected) {
        this.expected = expected;
    }

    @SafeVarargs
    public final void errors(ErrorMatcher... errorMatchers) {
        expected.expect(ConcernEffectException.class);
        expected.expect(new TypeSafeMatcher<ConcernEffectException>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("should found ConcernEffectException");
            }

            @Override
            protected boolean matchesSafely(ConcernEffectException exception) {
                final EffectResult effectResult = exception.getEffectResult();
                final List<EffectResult.Failure> errors = effectResult.errors();

                if (errorMatchers.length != errors.size()) {
                    return false;
                }

                for (int i = 0; i < errorMatchers.length; i++) {
                    if (!errorMatchers[i].verify(errors.get(i).getPropertyPath(), errors.get(i).getType())) {
                        return false;
                    }
                }

                return !effectResult.valid();
            }
        });
    }

    public static ValidationMatcher of(ExpectedException expected) {
        return new ValidationMatcher(expected);
    }

    public interface ErrorMatcher {
        boolean verify(String propertyPath, Object value);
    }
}
