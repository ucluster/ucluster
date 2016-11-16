package com.github.ucluster.feature.passwordless.authentication;

import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.User;
import com.github.ucluster.core.UserRepository;
import com.github.ucluster.core.authentication.AuthenticationResponse;
import com.github.ucluster.core.authentication.AuthenticationService;
import com.github.ucluster.mongo.Keys;
import com.github.ucluster.session.Session;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.ucluster.core.authentication.AuthenticationResponse.fail;
import static com.github.ucluster.core.authentication.AuthenticationResponse.success;

public class PasswordlessAuthenticationService implements AuthenticationService {

    @Inject
    Session session;

    @Inject
    private UserRepository users;

    private List<String> identities;

    public PasswordlessAuthenticationService() {
    }

    @Override
    public AuthenticationResponse authenticate(ApiRequest request) {
        final Optional<User> user = findUserByIdentity(request);

        if (!user.isPresent()) {
            return fail();
        }

        Optional<Object> confirmationCode = session.get(Keys.user_code(user.get()));

        if (!confirmationCode.isPresent()) {
            return fail(user);
        }

        Map<String, Object> confiramtions = (Map<String, Object>) confirmationCode.get();
        if (confiramtions.get("confirmation_code").equals(request.property("confirmation_code"))) {
            return success(user);
        }

        return fail(user);
    }

    private Optional<User> findUserByIdentity(ApiRequest request) {
        return identitiesOfRequest(request).stream()
                .map(identity -> users.findBy(identity, request.property(identity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private List<String> identitiesOfRequest(ApiRequest request) {
        return identities.stream()
                .filter(identity -> request.properties().containsKey(identity))
                .collect(Collectors.toList());
    }
}
