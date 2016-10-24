package com.github.ucluster.confirmation;

public interface ConfirmationService {

    void send(String target, String token);

    void confirm(String target, String token);
}