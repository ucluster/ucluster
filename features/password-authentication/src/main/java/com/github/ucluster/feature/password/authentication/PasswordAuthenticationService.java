package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ucluster.core.authentication.AuthenticationResponse.fail;
import static com.github.ucluster.core.authentication.AuthenticationResponse.success;

public class PasswordAuthenticationService implements AuthenticationService {
    @Inject
    UserRepository users;

    private List<String> identities;
    private String password;

    public PasswordAuthenticationService() {
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        final Optional<User> user = findUserByIdentity(request);

        if (!user.isPresent()) {
            return fail(Optional.empty());
        }

        if (passwordMatched(request, user.get())) {
            return success(user);
        }

        return fail(user);
    }

    private boolean passwordMatched(AuthenticationRequest request, User user) {
        final String storedPassword = String.valueOf(user.property(password).get().value());
        return Encryption.BCRYPT.check(String.valueOf(request.property(password)), storedPassword);
    }

    private Optional<User> findUserByIdentity(AuthenticationRequest request) {
        return identitiesOfRequest(request).stream()
                .map(identity -> users.findBy(identity, request.property(identity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private List<String> identitiesOfRequest(AuthenticationRequest request) {
        return identities.stream()
                .filter(identity -> request.properties().containsKey(identity))
                .collect(Collectors.toList());
    }
}
