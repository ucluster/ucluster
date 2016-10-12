package com.github.ucluster.test.framework.matcher;

import com.github.ucluster.core.Record;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecordMatcher {
    private final Record record;

    private RecordMatcher(Record record) {
        this.record = record;
    }

    public ValueMatcher prop(String path) {
        final Optional<Record.Property> property = record.property(path);
        if (!property.isPresent()) {
            return new ValueMatcher(Optional.empty());
        }
        return new ValueMatcher(Optional.ofNullable(property.get().value()));
    }

    public static RecordMatcher expect(Record record) {
        return new RecordMatcher(record);
    }

    public class ValueMatcher {
        private Optional<Object> actualValue;

        public ValueMatcher(Optional<Object> actualValue) {
            this.actualValue = actualValue;
        }

        public RecordMatcher value(Object value) {
            if (value == null) {
                assertThat(actualValue.isPresent(), is(false));
            } else {
                assertThat(actualValue.get(), is(value));
            }
            return RecordMatcher.this;
        }
    }
}
