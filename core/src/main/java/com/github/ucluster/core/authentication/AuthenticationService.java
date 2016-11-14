package com.github.ucluster.core.authentication;

import java.util.Map;

public interface AuthenticationService {
    AuthenticationResponse authenticate(Map<String, Object> request);
}
