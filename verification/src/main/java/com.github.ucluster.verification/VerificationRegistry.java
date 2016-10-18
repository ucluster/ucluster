package com.github.ucluster.verification;

import java.util.Optional;

public interface VerificationRegistry {

    Optional<VerificationService> find(String type);
}
