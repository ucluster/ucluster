package com.github.ucluster.core.authentication;

import com.github.ucluster.core.ApiRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(ApiRequest request);
}
