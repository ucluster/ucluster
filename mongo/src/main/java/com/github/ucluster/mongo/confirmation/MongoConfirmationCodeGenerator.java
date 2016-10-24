package com.github.ucluster.mongo.confirmation;

import com.github.ucluster.confirmation.ConfirmationCodeGenerator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class MongoConfirmationCodeGenerator implements ConfirmationCodeGenerator {
    private final int digits;

    public MongoConfirmationCodeGenerator(int digits) {
        this.digits = digits;
    }

    @Override
    public String generate() {
        return IntStream.range(0, digits).parallel()
                .mapToObj($ -> String.valueOf(ThreadLocalRandom.current().nextInt(10)))
                .reduce("", (a, b) -> a + b);
    }
}
