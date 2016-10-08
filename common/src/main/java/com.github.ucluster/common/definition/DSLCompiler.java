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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DSLCompiler {
    private static String DSL_COMPILER = "var definition = {};" +
            "var user = function (user) { definition = user; };" +
            "var request = function (request) { definition = request; }";

    public static <T extends Record> DefaultRecordDefinition<T> load(Injector injector, String script) {
        return new RecordDSL<T>(injector, loadRecordJsonDefinition(script)).load();
    }

    private static Map<String, Object> loadRecordJsonDefinition(String definition) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(definition);
            String recordDefinition = (String) engine.eval("JSON.stringify(definition)");
            return Json.fromJson(recordDefinition);
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
                    }, Names.named("update." + concernType + ".concern"))).getClass();

                    final Constructor<? extends Record.Property.Concern> constructor = propertyConcernClass.getConstructor(String.class, Object.class);
                    return constructor.newInstance(concernType, propertyProcessorConfiguration);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
