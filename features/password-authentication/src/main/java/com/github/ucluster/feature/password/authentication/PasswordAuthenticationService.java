package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.mongo.MongoProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.feature.password.authentication.PasswordAuthenticationService.PasswordAuthenticationResponse.fail;
import static com.github.ucluster.feature.password.authentication.PasswordAuthenticationService.PasswordAuthenticationResponse.success;

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
                //TODO: hide the MongoProperty, and can be used no matter which kind of db is used
                .map(identity -> users.findBy(new MongoProperty<>(identity, request.property(identity))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private List<String> identitiesOfRequest(AuthenticationRequest request) {
        return identities.stream()
                .filter(identity -> request.properties().containsKey(identity))
                .collect(Collectors.toList());
    }

    static class PasswordAuthenticationResponse implements AuthenticationResponse {
        private Status status = FAILED;
        private Optional<User> user = Optional.empty();

        public PasswordAuthenticationResponse(Optional<User> user, Status status) {
            this.user = user;
            this.status = status;
        }

        static PasswordAuthenticationResponse success(Optional<User> user) {
            return new PasswordAuthenticationResponse(user, SUCCEEDED);
        }

        static PasswordAuthenticationResponse fail(Optional<User> user) {
            return new PasswordAuthenticationResponse(user, FAILED);
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
