package com.github.ucluster.mongo.request;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRequest;
import com.github.ucluster.mongo.json.JsonRequest;
import com.github.ucluster.session.Session;
import org.mongodb.morphia.annotations.Transient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AuthenticationRequest extends MongoRequest implements Model {
    @Inject
    @Transient
    Session session;

    AuthenticationRequest() {
        super();
    }

    public AuthenticationRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public User.Request.Response approve(Map<String, Object> detail) {
        ensurePending();

        final JsonRequest request = JsonRequest.of(detail);
        ensureIdentityMatched(request);
        ensurePasswordMatched(request);

        status(Status.APPROVED);
        update();

        final String token = generateToken();
        saveTokenSession(token);
        return Response.builder().key("$TOKEN").value(token).get();
    }

    private void saveTokenSession(String token) {
        final Map<String, String> userSession = new HashMap<>();
        userSession.put("uuid", user.uuid());
        session.hmset(token, userSession);
        session.expire(token, 3600);

        session.set(user.uuid(), token);
    }

    private void ensureIdentityMatched(JsonRequest request) {
        final Optional<Property> identityProperty = user.property((String) request.property("identity_property"));
        ensurePropertyIsIdentity(identityProperty);

        if (!Objects.equals(request.property("identity_value"), identityProperty.get().value())) {
            failed();
        }
    }

    private void ensurePasswordMatched(JsonRequest request) {
        final Optional<Property> credentialProperty = user.property((String) request.property("credential_property"));
        ensurePropertyIsCredential(credentialProperty);

        if (!Encryption.BCRYPT.check(String.valueOf(request.property("credential_value")), String.valueOf(credentialProperty.get().value()))) {
            failed();
        }
    }

    private void ensurePropertyIsIdentity(Optional<Property> identityProperty) {
        if (!identityProperty.isPresent()) {
            failed();
        }

        final Object identity = user.definition().property(identityProperty.get().path()).definition().get("identity");
        if (!Objects.equals(true, identity)) {
            failed();
        }
    }

    private void ensurePropertyIsCredential(Optional<Property> credentialProperty) {
        if (!credentialProperty.isPresent()) {
            failed();
        }

        final Object identity = user.definition().property(credentialProperty.get().path()).definition().get("credential");
        if (!Objects.equals(true, identity)) {
            failed();
        }
    }

    private void failed() {
        status(Status.REJECTED);
        throw new RequestException();
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
