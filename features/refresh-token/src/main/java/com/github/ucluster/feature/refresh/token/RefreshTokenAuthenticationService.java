package com.github.ucluster.feature.refresh.token;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.session.Session;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.feature.refresh.token.RefreshTokenAuthenticationService.RefreshTokenAuthenticationResponse.fail;

public class RefreshTokenAuthenticationService implements AuthenticationService {

    @Inject
    Session session;

    @Inject
    private UserRepository users;

    private Object configuration;

    RefreshTokenAuthenticationService() {
    }

    public RefreshTokenAuthenticationService(Object configuration) {
        this.configuration = configuration;
    }

    @Override
    public AuthenticationResponse authenticate(Map<String, Object> request) {
        Optional<Map<String, Object>> refreshToken = refreshTokenInSession(refreshToken(request));

        if (!refreshToken.isPresent()) {
            return fail(Optional.empty());
        }

        if (!Objects.equals(refreshToken.get().get("access_token"), accessToken(request))) {
            return fail(Optional.empty());
        }

        Map<String, Object> user = (Map<String, Object>) session.get((String) refreshToken.get().get("access_token")).get();

        return RefreshTokenAuthenticationResponse.success(users.uuid((String) user.get("id")));
    }

    private Optional<Map<String, Object>> refreshTokenInSession(String refreshToken) {
        final Optional<Object> json = session.get(refreshToken);

        if (!json.isPresent()) {
            return Optional.empty();
        }

        return Optional.of((Map<String, Object>) json.get());
    }

    private String refreshToken(Map<String, Object> request) {
        return (String) properties(request).get("refresh_token");
    }

    private String accessToken(Map<String, Object> request) {
        return (String) properties(request).get("access_token");
    }

    private Map<String, Object> properties(Map<String, Object> request) {
        return (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
    }

    static class RefreshTokenAuthenticationResponse implements AuthenticationResponse {
        private Status status = FAILED;
        private Optional<User> user = Optional.empty();

        public RefreshTokenAuthenticationResponse(Optional<User> user, Status status) {
            this.user = user;
            this.status = status;
        }

        static RefreshTokenAuthenticationResponse success(Optional<User> user) {
            return new RefreshTokenAuthenticationResponse(user, SUCCEEDED);
        }

        static RefreshTokenAuthenticationResponse fail(Optional<User> user) {
            return new RefreshTokenAuthenticationResponse(user, FAILED);
        }

        @Override
        public Status status() {
            return status;
        }

        @Override
        public Optional<User> candidate() {
            return user;
        }
    }
}
