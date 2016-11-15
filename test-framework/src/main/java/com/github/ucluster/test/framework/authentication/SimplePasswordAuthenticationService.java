package com.github.ucluster.test.framework.authentication;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationRequest;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.mongo.MongoProperty;

import javax.inject.Inject;
import java.util.Optional;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.test.framework.authentication.SimplePasswordAuthenticationService.PasswordAuthenticationResponse.fail;
import static com.github.ucluster.test.framework.authentication.SimplePasswordAuthenticationService.PasswordAuthenticationResponse.success;

public class SimplePasswordAuthenticationService implements AuthenticationService {

    @Inject
    UserRepository users;

    public SimplePasswordAuthenticationService() {
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = String.valueOf(request.property("username"));
        String password = String.valueOf(request.property("password"));

        Optional<User> user = users.findBy(new MongoProperty<>("username", username));

        if (!user.isPresent()) {
            return fail(Optional.empty());
        }

        if (passwordMatched(user.get(), password)) {
            return success(user);
        }

        return fail(user);
    }

    private boolean passwordMatched(User user, String password) {
        final String storedPassword = String.valueOf(user.property("password").get().value());
        return Encryption.BCRYPT.check(password, storedPassword);
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
