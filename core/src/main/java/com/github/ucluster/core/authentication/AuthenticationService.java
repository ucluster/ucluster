package com.github.ucluster.core.authentication;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
