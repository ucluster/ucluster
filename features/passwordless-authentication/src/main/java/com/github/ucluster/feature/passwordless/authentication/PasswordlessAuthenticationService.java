package com.github.ucluster.feature.passwordless.authentication;

import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.mongo.MongoProperty;
import com.github.ucluster.session.Session;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.FAILED;
import static com.github.ucluster.core.authentication.AuthenticationResponse.Status.SUCCEEDED;
import static com.github.ucluster.feature.passwordless.authentication.PasswordlessAuthenticationService.PasswordlessAuthenticationResponse.fail;

public class PasswordlessAuthenticationService implements AuthenticationService {

    @Inject
    Session session;

    private Object configuration;
    
    @Inject
    private UserRepository users;

    PasswordlessAuthenticationService() {
    }

    public PasswordlessAuthenticationService(Object configuration) {
        this.configuration = configuration;
    }

    @Override
    public AuthenticationResponse authenticate(Map<String, Object> request) {
        final Optional<User> user = findUserByIdentity(request);

        if (!user.isPresent()) {
            return fail(Optional.empty());
        }

        Optional<Object> confirmationCode = session.get(user.get().uuid());

        if (!confirmationCode.isPresent()) {
            return fail(Optional.empty());
        }

        Map<String, Object> confiramtions = (Map<String, Object>) confirmationCode.get();
        if (confiramtions.get("confirmation_code").equals(properties(request).get("confirmation_code"))) {
            return PasswordlessAuthenticationResponse.success(user);
        }

        return fail(Optional.empty());
    }

    private Optional<User> findUserByIdentity(Map<String, Object> request) {
        return identitiesOfRequest(request).stream()
                //TODO: hide the MongoProperty, and can be used no matter which kind of db is used
                .map(identity -> users.findBy(new MongoProperty<>(identity, properties(request).get(identity))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private List<String> identityProperties() {
        return (List<String>) ((Map<String, Object>) configuration).get("identities");
    }
    
    private List<String> identitiesOfRequest(Map<String, Object> request) {
        return identityProperties().stream()
                .filter(properties(request)::containsKey)
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> properties(Map<String, Object> request) {
        return (Map<String, Object>) request.get("properties");
    }
    
    static class PasswordlessAuthenticationResponse implements AuthenticationResponse {
        private Status status = FAILED;
        private Optional<User> user = Optional.empty();

        public PasswordlessAuthenticationResponse(Optional<User> user, Status status) {
            this.user = user;
            this.status = status;
        }

        static PasswordlessAuthenticationResponse success(Optional<User> user) {
            return new PasswordlessAuthenticationResponse(user, SUCCEEDED);
        }

        static PasswordlessAuthenticationResponse fail(Optional<User> user) {
            return new PasswordlessAuthenticationResponse(user, FAILED);
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
