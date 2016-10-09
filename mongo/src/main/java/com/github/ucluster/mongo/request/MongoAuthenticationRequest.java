package com.github.ucluster.mongo.request;

import com.github.ucluster.common.concern.Encryption;
import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.MongoRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MongoAuthenticationRequest extends MongoRequest {
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
        ensureIdentityMatched(detail);
        ensurePasswordMatched(detail);

        status(Status.APPROVED);
        update();
    }

    private void ensurePasswordMatched(Map<String, Object> detail) {
        final Optional<Property> password = user.property("password");
        if (!Encryption.BCRYPT.check(String.valueOf(detail.get("password")), (String) password.get().value())) {
            failedAuthentication();
        }
    }

    private void ensureIdentityMatched(Map<String, Object> detail) {
        final Map<String, Object> identity = (Map<String, Object>) detail.get("identity");
        if (identity == null) {
            failedAuthentication();
        }

        final Optional<Property> identityProperty = user.property((String) identity.get("property"));
        ensurePropertyIsIdentity(identityProperty);

        if (!Objects.equals(identity.get("value"), identityProperty.get().value())) {
            failedAuthentication();
        }
    }

    private void ensurePropertyIsIdentity(Optional<Property> identityProperty) {
        if (!identityProperty.isPresent()) {
            failedAuthentication();
        }

        final Object identity = user.definition().property(identityProperty.get().path()).definition().get("identity");
        if (!Objects.equals(true, identity)) {
            failedAuthentication();
        }
    }

    private void failedAuthentication() {
        status(Status.REJECTED);
        throw new RequestException();
    }


    @Override
    public void reject(Map<String, Object> detail) {
        throw new RuntimeException("should not be called");
    }
}
