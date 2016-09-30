package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.PropertyProcessor;
import com.github.ucluster.core.definition.UserDefinitionRepository;
import com.github.ucluster.core.definition.ValidationResult;
import com.github.ucluster.core.exception.UserValidationException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MongoUserRepository implements Repository<User> {
    @Inject
    protected Datastore datastore;

    @Inject
    protected UserDefinitionRepository userDefinitions;

    @Override
    public User create(Record.Request request) {
        final MongoUser user = new MongoUser();
        user.createdAt = new DateTime();
        user.metadata = request.metadata();
        user.definition = userDefinitions.find(request.metadata());

        final User monitored = monitorLifecycle(user);
        request.properties().keySet().stream()
                .forEach(propertyPath -> monitored.update(new MongoUserProperty<>(
                        propertyPath,
                        request.properties().get(propertyPath))
                ));

        monitored.save();
        return monitored;
    }

    @Override
    public Optional<User> uuid(String uuid) {
        return enhance(datastore.get(MongoUser.class, new ObjectId(uuid)));
    }

    @Override
    public Optional<User> find(Record.Property property) {
        final MongoUser user = datastore.createQuery(MongoUser.class)
                .disableValidation()
                .field(MongoUserProperty.valueMongoField(property)).equal(property.value())
                .get();

        return enhance(user);
    }

    private Optional<User> enhance(MongoUser user) {
        if (user == null) {
            return Optional.empty();
        }

        user.definition = userDefinitions.find(user.metadata());
        return Optional.of(monitorLifecycle(user));
    }


    private User monitorLifecycle(User user) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MongoUser.class);

        enhancer.setCallback(new MethodInterceptor() {
            private DirtyTracker dirtyTracker = new DirtyTracker();

            @Override
            public Object intercept(Object instance, Method method, Object[] parameters, MethodProxy proxy) throws Throwable {
                if (isPropertySetterMethod(method)) {
                    recordDirtyProperty((Record.Property) parameters[0]);
                }

                if (isSaveMethod(method)) {
                    validate(user);
                    beforeSaveOn(user, PropertyProcessor.Type.BEFORE_CREATE);
                    save(user);
                    afterSave();
                }

                if (isUpdateMethod(method)) {
                    validate(user);
                    beforeSaveOn(user, PropertyProcessor.Type.BEFORE_UPDATE);
                    update(user);
                    afterSave();
                }


                return proxy.invoke(user, parameters);
            }

            private void afterSave() {
                dirtyTracker.clear();
            }

            private void validate(User user) {
                final ValidationResult result = userDefinitions.find(((MongoUser) user).metadata).validate(user, dirtyTracker.asArray());

                if (!result.valid()) {
                    throw new UserValidationException(result);
                }
            }

            private void beforeSaveOn(User user, PropertyProcessor.Type processType) {
                final Definition<User> definition = userDefinitions.find(((MongoUser) user).metadata);

                dirtyTracker.dirties().stream()
                        .filter(propertyPath -> dirtyTracker.isDirty(propertyPath))
                        .forEach(propertyPath -> {
                            Record.Property property = user.property(propertyPath).get();
                            property.value(definition.property(property.path()).process(processType, property).value());
                        });
            }

            private void save(User user) {
                datastore.save(user);
            }

            private void update(User user) {
                datastore.update(user, generateDirtyUpdateOperations());
            }

            private UpdateOperations<User> generateDirtyUpdateOperations() {
                final UpdateOperations<User> operations = datastore.createUpdateOperations(User.class)
                        .disableValidation();

                dirtyTracker.dirties().stream()
                        .map(user::property)
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

        return (MongoUser) enhancer.create();
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
