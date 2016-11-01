package com.github.ucluster.core;

import java.util.Map;

public interface AuthenticationService {

    User authenticate(Map<String, Object> request);
}
