package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

public interface PropertyValidator<T extends Record> extends Record.Property.Specification {

    ValidationResult validate(T record, String propertyPath);
}
