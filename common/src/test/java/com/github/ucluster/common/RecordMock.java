package com.github.ucluster.common;

import com.github.ucluster.core.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordMock {
    private Record record;
    private List<Record.Property> properties = new ArrayList<>();

    public RecordMock(Record record) {
        this.record = record;
    }

    public static RecordMock builder() {
        return new RecordMock(mock(Record.class));
    }

    public RecordMock.FieldBuilder path(String path) {
        return new FieldBuilder(path);
    }

    public Record get() {
        when(record.properties()).thenReturn(properties);
        return record;
    }

    public class FieldBuilder {
        private final String path;

        FieldBuilder(String path) {
            this.path = path;
        }

        public <T> RecordMock value(T value) {
            final Record.Property<T> property = mock(Record.Property.class);
            when(property.path()).thenReturn(path);
            when(property.value()).thenReturn(value);

            when(record.property(eq(path))).thenReturn(Optional.of(property));

            RecordMock.this.properties.add(property);

            return RecordMock.this;
        }

        public RecordMock none() {
            when(record.property(eq(path))).thenReturn(Optional.empty());

            return RecordMock.this;
        }
    }
}
