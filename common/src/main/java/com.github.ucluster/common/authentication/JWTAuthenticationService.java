package com.github.ucluster.common.authentication;

import com.github.ucluster.core.User;
import com.github.ucluster.core.authentication.AuthenticationLog;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.authentication.AuthenticationServiceRegistry;
import com.github.ucluster.core.authentication.TokenAuthenticationService;
import com.github.ucluster.core.exception.AuthenticationException;
import com.google.inject.Injector;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;

public class JWTAuthenticationService implements TokenAuthenticationService {
    @Inject
    AuthenticationServiceRegistry registry;

    @Inject
    Injector injector;

    @Override
    public String authenticate(Map<String, Object> request) {
        Optional<AuthenticationService> service = registry.find(methodOf(request));

        if (!service.isPresent()) {
            throw new AuthenticationException();
        }

        AuthenticationResponse response = service.get().authenticate(request);

        audit(request, response);

        if (response.status() == FAILED) {
            throw new AuthenticationException();
        }

        return issueToken(response);
    }

    @Override
    public Optional<User> verify(String token) {
        throw new RuntimeException("need to implement");
    }

    private String issueToken(AuthenticationResponse response) {
        return Jwts.builder()
                .setSubject((String) response.candidate().get().property("username").get().value())
                .signWith(SignatureAlgorithm.HS512, MacProvider.generateKey())
                .compact();
    }

    private void audit(Map<String, Object> request, AuthenticationResponse response) {
        AuthenticationLog authenticationLog = getAuthenticationLog(request, response).get();
        injector.injectMembers(authenticationLog);
        authenticationLog.save();
    }

    private Optional<AuthenticationLog> getAuthenticationLog(Map<String, Object> request, AuthenticationResponse response) {
        try {
            final Class<? extends AuthenticationLog> authenticationLogClass = injector.getInstance(AuthenticationLog.class).getClass();
            Constructor<? extends AuthenticationLog> constructor = authenticationLogClass.getConstructor(Map.class, AuthenticationResponse.class);
            return Optional.of(constructor.newInstance(request, response));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String methodOf(Map<String, Object> request) {
        Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
        return (String) metadata.getOrDefault("method", "password");
    }
}
