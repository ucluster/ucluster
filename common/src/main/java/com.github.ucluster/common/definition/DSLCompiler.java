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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DSLCompiler {
    private static String DSL_COMPILER = "var user_definition = {};" +
            "var user = function (user) { user_definition = user; };";

    public static UserDefinition load(Injector injector, String script) {
        final List<UserDefinition.PropertyDefinition> propertyDefinitions = new ArrayList<>();

        final Map<String, Object> userDefinitionJson = loadUserJsonDefinition(script);
        for (String propertyPath : userDefinitionJson.keySet()) {
            propertyDefinitions.add(loadPropertyDefinition(injector, userDefinitionJson, propertyPath));
        }

        return new DefaultUserDefinition(propertyDefinitions);
    }

    private static UserDefinition.PropertyDefinition loadPropertyDefinition(Injector injector, Map<String, Object> userDefinitionJson, String propertyPath) {
        final List<PropertyValidator> propertyValidators = new ArrayList<>();

        final Map<String, Object> propertyDefinitionJson = (Map<String, Object>) userDefinitionJson.get(propertyPath);

        for (String validatorType : propertyDefinitionJson.keySet()) {
            final PropertyValidator propertyValidator = loadPropertyValidator(injector, validatorType, propertyDefinitionJson.get(validatorType));
            if (propertyValidator != null) {
                injector.injectMembers(propertyValidator);
                propertyValidators.add(propertyValidator);
            }
        }

        final Map<String, Object> propertyMetadata = new HashMap<>();
        for (String metadataType : propertyDefinitionJson.keySet()) {
            if (!isValidator(injector, metadataType)) {
                propertyMetadata.put(metadataType, propertyDefinitionJson.get(metadataType));
            }
        }

        return new DefaultPropertyDefinition(propertyPath, propertyValidators, propertyMetadata);
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
}
