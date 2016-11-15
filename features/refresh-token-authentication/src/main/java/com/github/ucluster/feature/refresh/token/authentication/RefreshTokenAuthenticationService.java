package com.github.ucluster.feature.refresh.token.authentication;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.feature.refresh.token.authentication.RefreshTokenAuthenticationService.RefreshTokenAuthenticationResponse.fail;

public class RefreshTokenAuthenticationService implements AuthenticationService {

    @Inject
    private UserRepository users;

    public RefreshTokenAuthenticationService() {
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        final Optional<User> user = users.findByAccessToken(String.valueOf(request.property("access_token")));

        if (!user.isPresent()) {
            return fail(Optional.empty());
        }

        final Optional<User.Token> token = user.get().currentToken();
        if (!token.isPresent()) {
            return fail(Optional.empty());
        }

        if (!Objects.equals(token.get().refreshToken(), String.valueOf(request.property("refresh_token")))) {
            return fail(Optional.empty());
        }

        return RefreshTokenAuthenticationResponse.success(user);
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
