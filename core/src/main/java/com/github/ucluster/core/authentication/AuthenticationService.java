package com.github.ucluster.core.authentication;

import com.github.ucluster.core.request.AuthenticationRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
