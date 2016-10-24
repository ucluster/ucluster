package com.github.ucluster.mongo.confirmation;

import com.github.ucluster.confirmation.ConfirmationRegistry;
import com.github.ucluster.confirmation.ConfirmationService;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javax.inject.Inject;
import java.util.Optional;

public class MongoConfirmationRegistry implements ConfirmationRegistry {
    @Inject
    Injector injector;

    @Override
    public Optional<ConfirmationService> find(String type) {
        try {
            return Optional.ofNullable(injector.getInstance(Key.get(new TypeLiteral<ConfirmationService>() {
            }, Names.named("confirmation." + type + ".method"))));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
