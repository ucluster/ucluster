package com.github.ucluster.mongo.request;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRequest;
import com.github.ucluster.mongo.json.JsonRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MongoAuthenticationRequest extends MongoRequest implements Model {
    MongoAuthenticationRequest() {
        super();
    }

    public MongoAuthenticationRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean auto() {
        return true;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        ensurePending();

        final JsonRequest request = JsonRequest.of(detail);
        ensureIdentityMatched(request);
        ensurePasswordMatched(request);

        status(Status.APPROVED);
        update();
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
}
