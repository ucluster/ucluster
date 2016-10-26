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

    public static <T extends Record> DefaultRecordDefinition<T> load(Injector injector, String script) {
        return new RecordDSL<T>(injector, loadRecordJsonDefinition(script))
                .withConfirmation(loadConfirmationDefinition(script))
                .load();
    }

    public static <T extends Record> DefaultRecordDefinition<T> load_action(Injector injector, String script, String action) {
        return new RecordDSL<T>(injector, loadRecordActionJsonDefinition(script, action)).load();
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

    private static Map<String, Object> loadRecordActionJsonDefinition(String definition, String action) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(definition);
            String recordDefinition = (String) engine.eval("JSON.stringify(action_definition)");
            return (Map<String, Object>) Json.fromJson(recordDefinition).get(action);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, Object> loadConfirmationDefinition(String definition) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(definition);
            String recordDefinition = (String) engine.eval("JSON.stringify(confirmation_definition)");
            return Json.fromJson(recordDefinition);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class RecordDSL<R extends Record> {
        private final Injector injector;
        private final Map<String, Object> recordJson;
        private Map<String, Object> confirmationJson = new HashMap<>();

        RecordDSL(Injector injector, Map<String, Object> recordJson) {
            this.injector = injector;
            this.recordJson = recordJson;
        }

        DefaultRecordDefinition<R> load() {
            final List<Definition.PropertyDefinition<R>> propertyDefinitions =
                    recordJson.keySet().stream()
                            .map(propertyPath -> mapPropertyDSL(propertyPath).load())
                            .collect(Collectors.toList());

            List<Definition.Confirmation> confirmations = confirmationJson.keySet().stream()
                    .map(confirmingProperty -> mapConfirmationDSL(confirmingProperty).load())
                    .collect(Collectors.toList());

            return new DefaultRecordDefinition<>(propertyDefinitions, confirmations);
        }

        private ConfirmationDSL mapConfirmationDSL(String confirmation) {
            return new ConfirmationDSL(confirmation, confirmationJson.get(confirmation));
        }

        RecordDSL<R> withConfirmation(Map<String, Object> confirmationDefinitions) {
            this.confirmationJson = confirmationDefinitions;
            return this;
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

        private class ConfirmationDSL {
            private final String confirmingProperty;
            private final String usingMethod;

            public ConfirmationDSL(String confirmingProperty, Object usingMethod) {
                this.confirmingProperty = confirmingProperty;
                this.usingMethod = (String) usingMethod;
            }

            public Definition.Confirmation load() {
                return new Definition.Confirmation() {
                    @Override
                    public String target() {
                        return confirmingProperty;
                    }

                    @Override
                    public String method() {
                        return usingMethod;
                    }
                };
            }
        }
    }
}
