package com.github.ucluster.core.authentication;

import java.util.Map;

public interface AuthenticationRequestFactory {
    AuthenticationRequest create(Map<String, Object> request);
}
