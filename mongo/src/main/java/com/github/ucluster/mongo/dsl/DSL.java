package com.github.ucluster.mongo.dsl;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.core.definition.UserDefinition;
import com.github.ucluster.mongo.util.Json;
import com.github.ucluster.mongo.validator.DefaultPropertyDefinition;
import com.github.ucluster.mongo.validator.DefaultUserDefinition;
import com.github.ucluster.mongo.validator.FormatPropertyValidator;
import com.github.ucluster.mongo.validator.RequiredPropertyValidator;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DSL {
    public static String DSL_COMPILER = "var user_definition = {};\n" +
            "\n" +
            "var user = function (user) {\n" +
            "    user_definition = user;\n" +
            "};";

    public static UserDefinition load(String script) {
        final List<UserDefinition.PropertyDefinition> propertyDefinitions = new ArrayList<>();

        final Map<String, Object> userDefinitionJson = loadUserJsonDefinition(script);
        for (String propertyPath : userDefinitionJson.keySet()) {
            propertyDefinitions.add(loadPropertyDefinition(userDefinitionJson, propertyPath));
        }

        return new DefaultUserDefinition(propertyDefinitions);
    }

    private static UserDefinition.PropertyDefinition loadPropertyDefinition(Map<String, Object> userDefinitionJson, String propertyPath) {
        final List<PropertyValidator> propertyValidators = new ArrayList<>();

        final Map<String, Object> propertyDefinitionJson = (Map<String, Object>) userDefinitionJson.get(propertyPath);
        for (String validatorType : propertyDefinitionJson.keySet()) {
            final PropertyValidator propertyValidator = loadPropertyValidator(validatorType, propertyDefinitionJson.get(validatorType));
            if (propertyValidator != null) {
                propertyValidators.add(propertyValidator);
            }
        }

        return new DefaultPropertyDefinition(propertyPath, propertyValidators);
    }

    private static PropertyValidator loadPropertyValidator(String validatorType, Object propertyValidatorConfiguration) {
        switch (validatorType) {
            case "format":
                return new FormatPropertyValidator(propertyValidatorConfiguration);
            case "required":
                return new RequiredPropertyValidator(propertyValidatorConfiguration);
            default:
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
