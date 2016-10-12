package com.github.ucluster.mongo;

import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Map;

public class MongoRequestFactory implements RequestFactory {

    @Inject
    Injector injector;

    @Override
    public User.Request create(User user, Map<String, Object> request) {
        final Class<? extends User.Request> requestClass = getRequestClass(user, request);

        try {
            final Constructor<? extends User.Request> constructor = requestClass.getConstructor(User.class, Map.class);
            return constructor.newInstance(user, request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error to find request constructor", e);
        }
    }

    private Class<? extends User.Request> getRequestClass(User user, Map<String, Object> request) {
        try {
            return injector.getInstance(Key.get(new TypeLiteral<User.Request>() {
            }, Names.named("request." + type(request) + ".factory"))).getClass();
        } catch (Exception e) {
            throw new RecordTypeNotSupportedException((String) type(request));
        }
    }

    private Object type(Map<String, Object> request) {
        final Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return metadata.get("type");
    }
}
