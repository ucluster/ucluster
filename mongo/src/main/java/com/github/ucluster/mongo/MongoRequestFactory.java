package com.github.ucluster.mongo;

import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
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
        final Class<? extends User.Request> requestClass = injector.getInstance(Key.get(new TypeLiteral<User.Request>() {
        }, Names.named("request." + request.get("type") + ".factory"))).getClass();

        try {
            final Constructor<? extends User.Request> constructor = requestClass.getConstructor(User.class, Map.class);
            return constructor.newInstance(user, request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error to find request constructor", e);
        }
    }
}
