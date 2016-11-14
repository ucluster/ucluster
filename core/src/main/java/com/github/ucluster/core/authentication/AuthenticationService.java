package com.github.ucluster.core.authentication;

import com.github.ucluster.core.authentication.AuthenticationRequest.AuthenticationResponse;

import java.util.Map;

public interface AuthenticationService {
    AuthenticationResponse authenticate(Map<String, Object> request);
}
