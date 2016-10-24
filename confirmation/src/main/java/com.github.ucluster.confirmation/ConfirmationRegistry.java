package com.github.ucluster.confirmation;

import java.util.Optional;

public interface ConfirmationRegistry {

    Optional<ConfirmationService> find(String type);
}
