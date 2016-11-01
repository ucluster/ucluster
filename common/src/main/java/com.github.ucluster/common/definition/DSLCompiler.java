package com.github.ucluster.common.definition;

import com.github.ucluster.common.util.Json;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DSLCompiler {
    private static String DSL_COMPILER = ResourceReader.read("dsl_compiler.js");

    public static <T extends Record> DefaultRecordDefinition<T> load_user(Injector injector, String script) {
        return new RecordDSL<T>(injector, loadUserJsonDefinition(script)).load();
    }

    public static <T extends Record> DefaultRecordDefinition<T> load_request(Injector injector, String script, String type) {
        final Map<String, Object> requestDefinitionJson = loadRequestJsonDefinition(script, type);
        if (requestDefinitionJson == null || requestDefinitionJson.isEmpty()) {
            return null;
        }

        return new RecordDSL<T>(injector, requestDefinitionJson).load();
    }

    public static Map<String, Object> load_auth_config(String script, String type) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(script);
            String config = (String) engine.eval("JSON.stringify(auth_methods)");
            return (Map<String, Object>) Json.fromJson(config).getOrDefault(type, new HashMap<>());
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, Object> loadUserJsonDefinition(String script) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(script);
            String recordDefinition = (String) engine.eval("JSON.stringify(definition)");
            return Json.fromJson(recordDefinition);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, Object> loadRequestJsonDefinition(String script, String type) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(script);
            String recordDefinition = (String) engine.eval("JSON.stringify(request_definitions)");
            return (Map<String, Object>) Json.fromJson(recordDefinition).getOrDefault(type, new HashMap<>());
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class RecordDSL<R extends Record> {
        private final Injector injector;
        private final Map<String, Object> recordJson;

        RecordDSL(Injector injector, Map<String, Object> recordJson) {
            this.injector = injector;
            this.recordJson = recordJson;
        }

        DefaultRecordDefinition<R> load() {
            final List<Definition.PropertyDefinition<R>> propertyDefinitions =
                    recordJson.keySet().stream()
                            .map(propertyPath -> mapPropertyDSL(propertyPath).load())
                            .collect(Collectors.toList());

            return new DefaultRecordDefinition<>(propertyDefinitions);
        }

        private PropertyDSL<R> mapPropertyDSL(String propertyPath) {
            return new PropertyDSL<>(propertyPath, (Map<String, Object>) recordJson.get(propertyPath));
        }

        private class PropertyDSL<P extends Record> {

            private final String propertyPath;
            private final Map<String, Object> propertyJson;

            PropertyDSL(String propertyPath, Map<String, Object> propertyJson) {
                this.propertyPath = propertyPath;
                this.propertyJson = propertyJson;
            }

            private DefaultRecordDefinition.PropertyDefinition<P> load() {
                return new DefaultPropertyDefinition<>(propertyPath, propertyConcerns());
            }

            private Collection<Record.Property.Concern> propertyConcerns() {
                return propertyJson.keySet().stream()
                        .map(key -> {
                            final Record.Property.Concern propertyConcern = loadPropertyConcern(key, propertyJson.get(key));
                            injector.injectMembers(propertyConcern);
                            return propertyConcern;
                        }).collect(Collectors.toList());
            }

            private Record.Property.Concern loadPropertyConcern(String concernType, Object propertyProcessorConfiguration) {
                try {
                    final Class<? extends Record.Property.Concern> propertyConcernClass = injector.getInstance(Key.get(new TypeLiteral<Record.Property.Concern>() {
                    }, Names.named("property." + concernType + ".concern"))).getClass();

                    final Constructor<? extends Record.Property.Concern> constructor = propertyConcernClass.getConstructor(String.class, Object.class);
                    return constructor.newInstance(concernType, propertyProcessorConfiguration);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
