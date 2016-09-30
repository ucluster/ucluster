package com.github.ucluster.common.definition;

import com.github.ucluster.common.util.Json;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
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
    private static String DSL_COMPILER = "var user_definition = {};" +
            "var user = function (user) { user_definition = user; };";

    public static DefaultUserDefinition load(Injector injector, String script) {
        return new UserDSL(injector, loadUserJsonDefinition(script)).load();
    }

    private static Map<String, Object> loadUserJsonDefinition(String definition) {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        try {
            engine.eval(DSL_COMPILER);
            engine.eval(definition);
            String user_definition = (String) engine.eval("JSON.stringify(user_definition)");
            return Json.fromJson(user_definition);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class UserDSL {
        private final Injector injector;
        private final Map<String, Object> userJson;

        UserDSL(Injector injector, Map<String, Object> userJson) {
            this.injector = injector;
            this.userJson = userJson;
        }

        DefaultUserDefinition load() {
            final List<Definition.PropertyDefinition<User>> propertyDefinitions =
                    userJson.keySet().stream()
                            .map(propertyPath -> mapPropertyDSL(propertyPath).load())
                            .collect(Collectors.toList());

            return new DefaultUserDefinition(propertyDefinitions);
        }

        private PropertyDSL mapPropertyDSL(String propertyPath) {
            return new PropertyDSL(propertyPath, (Map<String, Object>) userJson.get(propertyPath));
        }

        private class PropertyDSL {

            private final String propertyPath;
            private final Map<String, Object> propertyJson;

            PropertyDSL(String propertyPath, Map<String, Object> propertyJson) {
                this.propertyPath = propertyPath;
                this.propertyJson = propertyJson;
            }

            private DefaultUserDefinition.PropertyDefinition<User> load() {
                return new DefaultPropertyDefinition(propertyPath, propertyConcerns());
            }

            private Collection<Record.Property.Concern<User>> propertyConcerns() {
                return propertyJson.keySet().stream()
                        .map(key -> {
                            final Record.Property.Concern<User> propertyConcern = loadPropertyConcern(key, propertyJson.get(key));
                            injector.injectMembers(propertyConcern);
                            return propertyConcern;
                        }).collect(Collectors.toList());
            }

            private Record.Property.Concern<User> loadPropertyConcern(String concernType, Object propertyProcessorConfiguration) {
                try {
                    final Class<? extends Record.Property.Concern<User>> propertyConcernClass =
                            injector.getInstance(Key.get(new TypeLiteral<Class<? extends Record.Property.Concern<User>>>() {
                            }, Names.named("property." + concernType + ".concern")));

                    final Constructor<? extends Record.Property.Concern<User>> constructor = propertyConcernClass.getConstructor(String.class, Object.class);
                    return constructor.newInstance(concernType, propertyProcessorConfiguration);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
