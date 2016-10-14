package com.github.ucluster.mongo.request;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRequest;

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
        ensureIdentityMatched((Map<String, Object>) detail.get("properties"));
        ensurePasswordMatched((Map<String, Object>) detail.get("properties"));

        status(Status.APPROVED);
        update();
    }

    private void ensurePasswordMatched(Map<String, Object> detail) {
        final Optional<Property> credentialProperty = user.property((String) detail.get("credential_property"));
        ensurePropertyIsCredential(credentialProperty);

        if (!Encryption.BCRYPT.check(String.valueOf(detail.get("credential_value")), String.valueOf(credentialProperty.get().value()))) {
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

    private void ensureIdentityMatched(Map<String, Object> detail) {
        final Optional<Property> identityProperty = user.property((String) detail.get("identity_property"));
        ensurePropertyIsIdentity(identityProperty);

        if (!Objects.equals(detail.get("identity_value"), identityProperty.get().value())) {
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

    private void failed() {
        status(Status.REJECTED);
        throw new RequestException();
    }
}
