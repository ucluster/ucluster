package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.MongoRequest;
import com.github.ucluster.session.Session;
import org.mongodb.morphia.annotations.Transient;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MongoRecoveryRequest extends MongoRequest {
    @Inject
    @Transient
    Session session;

    MongoRecoveryRequest() {
    }

    public MongoRecoveryRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean autoApprovable() {
        return true;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        ensureOttMatched(detail);

        final Map<String, Object> credential = (Map<String, Object>) detail.get("credential");
        if (credential == null) {
            failed();
        }

        final Property credentialProperty = getCredentialProperty(detail);
        credentialProperty.value(credential.get("value"));
        user.property(credentialProperty);
        user.update();


        status(Status.APPROVED);
        update();
    }

    private void ensureOttMatched(Map<String, Object> detail) {
        final String ott = String.valueOf(detail.get("ott"));

        final Optional<Object> ottInSession = session.get(user.uuid() + ":ott");
        if (!ottInSession.isPresent()) {
            failed();
        }

        if (!Objects.equals(ott, String.valueOf(ottInSession.get()))) {
            failed();
        }
    }

    private Property getCredentialProperty(Map<String, Object> detail) {
        final Map<String, Object> credential = (Map<String, Object>) detail.get("credential");
        if (credential == null) {
            failed();
        }

        final Optional<Property> credentialProperty = user.property((String) credential.get("property"));
        if (!credentialProperty.isPresent()) {
            failed();
        }
        ensurePropertyIsCredential(credentialProperty);

        return credentialProperty.get();
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
