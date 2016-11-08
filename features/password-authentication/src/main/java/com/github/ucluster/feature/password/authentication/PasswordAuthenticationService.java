package com.github.ucluster.feature.password.authentication;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.AuthenticationService;
import com.github.ucluster.core.Repository;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.AuthenticationException;
import com.github.ucluster.mongo.MongoProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PasswordAuthenticationService implements AuthenticationService {
    @Inject
    Repository<User> users;

    private Object configuration;

    PasswordAuthenticationService() {
    }

    public PasswordAuthenticationService(Object configuration) {
        this.configuration = configuration;
    }

    @Override
    public User authenticate(Map<String, Object> request) {
        final User user = findUserByIdentity(request);
        ensurePasswordMatched(request, user);
        return user;
    }

    private void ensurePasswordMatched(Map<String, Object> request, User user) {
        final String storedPassword = String.valueOf(user.property(passwordProperty()).get().value());
        if (!Encryption.BCRYPT.check(String.valueOf(request.get("password")), storedPassword)) {
            throw new AuthenticationException();
        }
    }

    private User findUserByIdentity(Map<String, Object> request) {
        final Optional<User> user = identitiesOfRequest(request).stream()
                //TODO: hide the MongoProperty, and can be used no matter which kind of db is used
                .map(identity -> users.findBy(new MongoProperty<>(identity, request.get(identity))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (!user.isPresent()) {
            throw new AuthenticationException();
        }

        return user.get();
    }

    private List<String> identityProperties() {
        return (List<String>) ((Map<String, Object>) configuration).get("identities");
    }

    private String passwordProperty() {
        return (String) ((Map<String, Object>) configuration).get("password");
    }

    private List<String> identitiesOfRequest(Map<String, Object> request) {
        return identityProperties().stream()
                .filter(request::containsKey)
                .collect(Collectors.toList());
    }
}
