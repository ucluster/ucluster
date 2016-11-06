package com.github.ucluster.mongo.feature;

import com.github.ucluster.common.definition.DSLCompiler;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.feature.Feature;
import com.github.ucluster.mongo.dsl.MongoDSLScript;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MongoFeature extends MongoDSLScript implements Feature {
    @Inject
    protected Injector injector;

    protected Map<String, Class<? extends User.Request>> requestBindings = new HashMap<>();

    MongoFeature() {
        super();
    }

    public MongoFeature(String scriptType, String script) {
        super(scriptType, script);
    }

    public MongoFeature(String userType, String scriptType, String script) {
        super(userType, scriptType, script);
    }

    @Override
    public <D extends Record> Optional<Definition<D>> definition(Class<D> klass) {
        return definition(klass, new HashMap<String, Object>());
    }

    @Override
    public <D extends Record> Optional<Definition<D>> definition(Class<D> klass, Map<String, Object> configuration) {
        return Optional.of(DSLCompiler.load_user(injector, script));
    }

    @Override
    public <D extends Record> Optional<Definition<D>> definition(Class<D> klass, String name) {
        return definition(klass, name, new HashMap<>());
    }

    @Override
    public <D extends Record> Optional<Definition<D>> definition(Class<D> klass, String name, Map<String, Object> configuration) {
        return Optional.ofNullable(DSLCompiler.load_request(injector, script, name));
    }

    @Override
    public <D extends Record> Optional<Class<? extends D>> bindingOf(Class<D> klass, String name) {
        final Class<? extends D> aClass = (Class<? extends D>) requestBindings.get(name);
        return Optional.ofNullable(aClass);
    }

    public MongoFeature bind(Class<? extends User.Request> klass, String name) {
        requestBindings.put(name, klass);
        return this;
    }
}
