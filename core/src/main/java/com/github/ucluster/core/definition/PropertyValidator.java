package com.github.ucluster.core.definition;

import com.github.ucluster.core.Record;

public interface PropertyValidator<T extends Record> extends Record.Property.Concern {

    EffectResult validate(T record, String propertyPath);
}
