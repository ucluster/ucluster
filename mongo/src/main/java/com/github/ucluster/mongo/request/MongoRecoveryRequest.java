package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.core.exception.RequestException;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoRequest;
import com.github.ucluster.mongo.json.JsonRequest;
import com.github.ucluster.session.Session;
import org.mongodb.morphia.annotations.Transient;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MongoRecoveryRequest extends MongoRequest implements Model {
    @Inject
    @Transient
    Session session;

    MongoRecoveryRequest() {
    }

    public MongoRecoveryRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean auto() {
        return true;
    }

    @Override
    public void approve(Map<String, Object> detail) {
        ensurePending();
        ensureOttMatched(detail);

        final JsonRequest request = JsonRequest.of(detail);
        ensurePropertyIsCredential((String) request.property("credential_property"));

        user.property((String) request.property("credential_property"), request.property("credential_value"));
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

    private void ensurePropertyIsCredential(String path) {
        final Object identity = user.definition().property(path).definition().get("credential");
        if (!Objects.equals(true, identity)) {
            failed();
        }
    }

    private void failed() {
        status(Status.REJECTED);
        throw new RequestException();
    }
}
