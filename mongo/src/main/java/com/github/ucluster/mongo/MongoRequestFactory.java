package com.github.ucluster.mongo;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RecordTypeNotSupportedException;
import com.github.ucluster.core.feature.FeatureRepository;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Optional;

public class MongoRequestFactory implements RequestFactory {

    @Inject
    protected Injector injector;

    @Inject
    protected FeatureRepository features;

    @Override
    public User.Request create(User user, ApiRequest request) {
        final Class<? extends User.Request> requestClass = getRequestClass(user, request);

        try {
            final Constructor<? extends User.Request> constructor = requestClass.getConstructor(User.class, ApiRequest.class);
            return constructor.newInstance(user, request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error to find request constructor", e);
        }
    }

    private Class<? extends User.Request> getRequestClass(User user, ApiRequest request) {
        final Optional<? extends Class<? extends User.Request>> klass = features.features(new HashMap<>())
                .stream()
                .map(feature -> feature.bindingOf(User.Request.class, request.metadata().type()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        return klass.orElseThrow(() -> new RecordTypeNotSupportedException(request.metadata().type()));
    }
}
