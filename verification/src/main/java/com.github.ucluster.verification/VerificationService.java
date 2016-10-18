package com.github.ucluster.verification;

public interface VerificationService {

    void send(String target, String token);

    void verify(String target, String token);
}