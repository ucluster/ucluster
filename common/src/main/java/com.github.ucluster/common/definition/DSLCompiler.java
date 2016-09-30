package com.github.ucluster.common.definition;

import com.github.ucluster.common.definition.util.Json;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.PropertyValidator;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
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
            final List<DefaultUserDefinition.PropertyDefinition> propertyDefinitions = userJson.keySet().stream()
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

            private DefaultUserDefinition.PropertyDefinition load() {
                return new DefaultPropertyDefinition(propertyPath, propertyValidators(), propertyProcessor());
            }

            private List<PropertyProcessor> propertyProcessor() {
                return propertyJson.keySet().stream()
                        .filter(this::isProcessor)
                        .map(key -> {
                            final PropertyProcessor propertyProcessor = loadPropertyProcessor(key, propertyJson.get(key));
                            injector.injectMembers(propertyProcessor);
                            return propertyProcessor;
                        }).collect(Collectors.toList());
            }

            private List<PropertyValidator> propertyValidators() {
                return propertyJson.keySet().stream()
                        .filter(this::isValidator)
                        .map(key -> {
                            final PropertyValidator propertyValidator = loadPropertyValidator(key, propertyJson.get(key));
                            injector.injectMembers(propertyValidator);
                            return propertyValidator;
                        }).collect(Collectors.toList());
            }

            private boolean isValidator(String type) {
                try {
                    injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + type + ".validator")));

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            private boolean isProcessor(String type) {
                try {
                    injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + type + ".processor")));

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            private PropertyProcessor loadPropertyProcessor(String processorType, Object propertyProcessorConfiguration) {
                try {
                    final Class propertyProcessorClass = injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + processorType + ".processor")));

                    final Constructor<PropertyProcessor> constructor = propertyProcessorClass.getConstructor(String.class, Object.class);
                    return constructor.newInstance(processorType, propertyProcessorConfiguration);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private PropertyValidator loadPropertyValidator(String validatorType, Object propertyValidatorConfiguration) {
                try {
                    final Class propertyValidatorClass = injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + validatorType + ".validator")));

                    final Constructor<PropertyValidator> constructor = propertyValidatorClass.getConstructor(String.class, Object.class);
                    return constructor.newInstance(validatorType, propertyValidatorConfiguration);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
