package com.github.ucluster.mongo.verification;

import com.github.ucluster.verification.VerificationCodeGenerator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class MongoVerificationCodeGenerator implements VerificationCodeGenerator {
    private final int digits;

    public MongoVerificationCodeGenerator(int digits) {
        this.digits = digits;
    }

    @Override
    public String generate() {
        return IntStream.range(0, digits).parallel()
                .mapToObj($ -> String.valueOf(ThreadLocalRandom.current().nextInt(10)))
                .reduce("", (a, b) -> a + b);
    }
}
