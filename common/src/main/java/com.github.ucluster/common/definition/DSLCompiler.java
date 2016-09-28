package com.github.ucluster.common.definition;

import com.github.ucluster.common.definition.util.Json;
import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
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

    public static UserDefinition load(Injector injector, String script) {
        return UserDSL.load(injector, loadUserJsonDefinition(script));
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
        static UserDefinition load(Injector injector, Map<String, Object> json) {
            final List<UserDefinition.PropertyDefinition> propertyDefinitions = json.keySet().stream()
                    .map(propertyPath -> PropertyDSL.loadPropertyDefinition(injector, json, propertyPath))
                    .collect(Collectors.toList());

            return new DefaultUserDefinition(propertyDefinitions);
        }

        private static class PropertyDSL {

            private static UserDefinition.PropertyDefinition loadPropertyDefinition(Injector injector, Map<String, Object> userDefinitionJson, String propertyPath) {
                final Map<String, Object> propertyDefinitionJson = (Map<String, Object>) userDefinitionJson.get(propertyPath);

                return new DefaultPropertyDefinition(propertyPath, loadPropertyValidators(injector, propertyDefinitionJson), loadPropertyMetadata(injector, propertyDefinitionJson));
            }

            private static Map<String, Object> loadPropertyMetadata(Injector injector, Map<String, Object> json) {
                return json.keySet().stream()
                        .filter(key -> !isValidator(injector, key))
                        .collect(Collectors.toMap(key -> key, json::get));
            }

            private static List<PropertyValidator> loadPropertyValidators(Injector injector, Map<String, Object> json) {
                return json.keySet().stream()
                        .filter(key -> isValidator(injector, key))
                        .map(key -> {
                            final PropertyValidator propertyValidator = loadPropertyValidator(injector, key, json.get(key));
                            injector.injectMembers(propertyValidator);
                            return propertyValidator;
                        }).collect(Collectors.toList());
            }

            private static boolean isValidator(Injector injector, String type) {
                try {
                    injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + type + ".validator")));

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            private static PropertyValidator loadPropertyValidator(Injector injector, String validatorType, Object propertyValidatorConfiguration) {
                try {
                    final Class propertyValidatorClass = injector.getInstance(Key.get(new TypeLiteral<Class>() {
                    }, Names.named("property." + validatorType + ".validator")));

                    final Constructor<PropertyValidator> constructor = propertyValidatorClass.getConstructor(Object.class);
                    return constructor.newInstance(propertyValidatorConfiguration);
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }


}
