package com.github.ucluster.feature.refresh.token;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.session.Session;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.feature.refresh.token.RefreshTokenAuthenticationService.RefreshTokenAuthenticationResponse.fail;

public class RefreshTokenAuthenticationService implements AuthenticationService {
    
    @Inject
    Session session;
    
    private Object configuration;

    @Inject
    private UserRepository users;

    RefreshTokenAuthenticationService() {
    }

    public RefreshTokenAuthenticationService(Object configuration) {
        this.configuration = configuration;
    }

    @Override
    public AuthenticationResponse authenticate(Map<String, Object> request) {
        Map<String, Object> properties = (Map<String, Object>) request.getOrDefault("properties", new HashMap<>());
        String refreshToken = (String) properties.get("refresh_token");
        Optional<Object> tokens = session.get(refreshToken);

        if (!tokens.isPresent()) {
            return fail(Optional.empty());
        }

        Map<String, Object> json = (Map<String, Object>) tokens.get();

        if (!json.get("access_token").equals(properties.get("access_token"))) {
            return fail(Optional.empty());
        }

        Map<String, Object> user = (Map<String, Object>) session.get((String) json.get("access_token")).get();

        return RefreshTokenAuthenticationResponse.success(users.uuid((String) user.get("id")));
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
