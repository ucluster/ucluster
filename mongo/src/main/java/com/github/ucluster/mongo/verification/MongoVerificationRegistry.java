package com.github.ucluster.mongo.verification;

import com.github.ucluster.verification.VerificationRegistry;
import com.github.ucluster.verification.VerificationService;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import javax.inject.Inject;
import java.util.Optional;

public class MongoVerificationRegistry implements VerificationRegistry {
    @Inject
    Injector injector;

    @Override
    public Optional<VerificationService> find(String type) {
        try {
            return Optional.ofNullable(injector.getInstance(Key.get(new TypeLiteral<VerificationService>() {
            }, Names.named("verification." + type + ".method"))));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
