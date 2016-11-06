package com.github.ucluster.core.feature;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;

import java.util.Map;
import java.util.Optional;

public interface Feature {

    <D extends Record> Optional<Definition<D>> definition(Class<D> klass);

    <D extends Record> Optional<Definition<D>> definition(Class<D> klass, Map<String, Object> configuration);

    <D extends Record> Optional<Definition<D>> definition(Class<D> klass, String name);

    <D extends Record> Optional<Definition<D>> definition(Class<D> klass, String name, Map<String, Object> configuration);

    <D extends Record> Optional<Class<? extends D>> bindingOf(Class<D> klass, String name);
}