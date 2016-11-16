package com.github.ucluster.feature.refresh.token.authentication;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.core.request.AuthenticationRequest;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.fail;
import static com.github.ucluster.core.authentication.AuthenticationResponse.success;

public class RefreshTokenAuthenticationService implements AuthenticationService {

    @Inject
    private UserRepository users;

    public RefreshTokenAuthenticationService() {
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        final Optional<User> user = users.findByAccessToken(String.valueOf(request.property("access_token")));

        if (!user.isPresent()) {
            return fail();
        }

        final Optional<User.Token> token = user.get().currentToken();
        if (!token.isPresent()) {
            return fail(user);
        }

        if (!Objects.equals(token.get().refreshToken(), String.valueOf(request.property("refresh_token")))) {
            return fail(user);
        }

        return success(user);
    }
}
