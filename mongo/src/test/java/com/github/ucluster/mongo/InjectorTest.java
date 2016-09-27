package com.github.ucluster.mongo;

import com.github.ucluster.core.definition.PropertyValidator;
import com.github.ucluster.mongo.definition.FormatValidator;
import com.github.ucluster.mongo.definition.RequiredValidator;
import com.github.ucluster.mongo.definition.UniquenessValidator;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.google.inject.Guice.createInjector;

public class InjectorTest {
    private Injector injector;

    @Test
    public void should_get_by_name() {
        injector = createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        registerValidator("property.format.validator", FormatValidator.class);
                        registerValidator("property.required.validator", RequiredValidator.class);
                        registerValidator("property.uniqueness.validator", UniquenessValidator.class);
                    }

                    private void registerValidator(String key, Class<? extends PropertyValidator> propertyValidatorClass) {
                        bind(new TypeLiteral<Class>() {
                        }).annotatedWith(Names.named(key)).toInstance(propertyValidatorClass);
                    }
                });

        final Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();

        allBindings.forEach((key, value) -> {
            System.out.println(key);
            System.out.println(value);
        });

        final Class<PropertyValidator> instance = injector.getInstance(Key.get(new TypeLiteral<Class>() {
        }, Names.named("property." + "required" + ".validator")));


        try {
            final Constructor<PropertyValidator> constructor = instance.getConstructor(Object.class);
            final PropertyValidator propertyValidator = constructor.newInstance(false);

            final RequiredValidator required = (RequiredValidator) propertyValidator;
            System.out.println(required.configuration());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
