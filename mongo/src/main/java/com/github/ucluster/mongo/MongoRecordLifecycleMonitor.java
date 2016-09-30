package com.github.ucluster.mongo;

import com.github.ucluster.core.LifecycleMonitor;
import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MongoRecordLifecycleMonitor<T extends Record> implements LifecycleMonitor<T> {
    @Inject
    protected Datastore datastore;

    @Inject
    protected DefinitionRepository<Definition<T>> definitions;

    @Override
    public T monitor(T record) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(record.getClass());

        enhancer.setCallback(new MethodInterceptor() {
            private DirtyTracker dirtyTracker = new DirtyTracker();

            @Override
            public Object intercept(Object instance, Method method, Object[] parameters, MethodProxy proxy) throws Throwable {
                if (isPropertySetterMethod(method)) {
                    recordDirtyProperty((Record.Property) parameters[0]);
                }

                if (isSaveMethod(method)) {
                    validate(record);
                    beforeSaveOn(record, PropertyProcessor.Type.BEFORE_CREATE);
                    save(record);
                    afterSave();
                }

                if (isUpdateMethod(method)) {
                    validate(record);
                    beforeSaveOn(record, PropertyProcessor.Type.BEFORE_UPDATE);
                    update(record);
                    afterSave();
                }


                return proxy.invoke(record, parameters);
            }

            private void afterSave() {
                dirtyTracker.clear();
            }

            private void validate(T record) {
                final ValidationResult result = definitions.find(((MongoUser) record).metadata).validate(record, dirtyTracker.asArray());

                if (!result.valid()) {
                    throw new UserValidationException(result);
                }
            }

            private void beforeSaveOn(T record, PropertyProcessor.Type processType) {
                final Definition<T> definition = definitions.find(((MongoUser) record).metadata);

                dirtyTracker.dirties().stream()
                        .filter(propertyPath -> dirtyTracker.isDirty(propertyPath))
                        .forEach(propertyPath -> {
                            Record.Property property = record.property(propertyPath).get();
                            property.value(definition.property(property.path()).process(processType, property).value());
                        });
            }

            private void save(T record) {
                datastore.save(record);
            }

            private void update(T record) {
                datastore.update(record, generateDirtyUpdateOperations(record));
            }

            private UpdateOperations<Record> generateDirtyUpdateOperations(T record) {
                final UpdateOperations<Record> operations = datastore.createUpdateOperations(Record.class)
                        .disableValidation();

                dirtyTracker.dirties().stream()
                        .map(record::property)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(property ->
                                operations.set(MongoUserProperty.mongoField(property), property)
                        );

                return operations;
            }

            private boolean isSaveMethod(Method method) {
                return isSaveMethodNameMatched(method) && isSaveMethodParameterTypeMatched(method);
            }

            private boolean isSaveMethodParameterTypeMatched(Method method) {
                return method.getParameterTypes().length == 0;
            }

            private boolean isSaveMethodNameMatched(Method method) {
                return "save".equals(method.getName());
            }

            private boolean isUpdateMethod(Method method) {
                return isUpdateMethodNameMatched(method) && isUpdateMethodParameterTypeMatched(method);
            }

            private boolean isUpdateMethodParameterTypeMatched(Method method) {
                return method.getParameterTypes().length == 0;
            }

            private boolean isUpdateMethodNameMatched(Method method) {
                return "update".equals(method.getName());
            }

            private boolean isPropertySetterMethod(Method method) {
                return isPropertySetterMethodNameMatched(method) && isPropertySetterMethodParameterTypeMatched(method);
            }

            private boolean isPropertySetterMethodParameterTypeMatched(Method method) {
                return method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Record.Property.class);
            }

            private boolean isPropertySetterMethodNameMatched(Method method) {
                return "update".equals(method.getName());
            }

            private void recordDirtyProperty(Record.Property property) {
                dirtyTracker.dirty(property.path());
            }
        });

        return (T) enhancer.create();
    }


    private static class DirtyTracker {
        Set<String> dirtyProperties = new HashSet<>();

        void dirty(String propertyPath) {
            dirtyProperties.add(propertyPath);
        }

        boolean isDirty(String propertyPath) {
            return dirtyProperties.contains(propertyPath);
        }

        Collection<String> dirties() {
            return dirtyProperties;
        }

        String[] asArray() {
            return dirtyProperties.toArray(new String[dirtyProperties.size()]);
        }

        void clear() {
            dirtyProperties.clear();
        }
    }
}
