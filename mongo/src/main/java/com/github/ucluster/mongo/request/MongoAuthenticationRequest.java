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
    }

    public MongoAuthenticationRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean autoApprovable() {
        return true;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        ensurePending();
        ensureIdentityMatched(detail);
        ensurePasswordMatched(detail);

        status(Status.APPROVED);
        update();
    }

    private void ensurePasswordMatched(Map<String, Object> detail) {
        final Map<String, Object> credential = (Map<String, Object>) detail.get("credential");
        if (credential == null) {
            failed();
        }

        final Optional<Property> credentialProperty = user.property((String) credential.get("property"));
        ensurePropertyIsCredential(credentialProperty);

        if (!Encryption.BCRYPT.check(String.valueOf(credential.get("value")), String.valueOf(credentialProperty.get().value()))) {
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
        final Map<String, Object> identity = (Map<String, Object>) detail.get("identity");
        if (identity == null) {
            failed();
        }

        final Optional<Property> identityProperty = user.property((String) identity.get("property"));
        ensurePropertyIsIdentity(identityProperty);

        if (!Objects.equals(identity.get("value"), identityProperty.get().value())) {
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
