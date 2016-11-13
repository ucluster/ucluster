package com.github.ucluster.core.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
}
