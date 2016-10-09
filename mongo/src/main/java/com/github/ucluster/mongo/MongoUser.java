package com.github.ucluster.mongo;

import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity("users")
@Converters(JodaDateTimeConverter.class)
public class MongoUser extends MongoRecord<User> implements User {

    @Inject
    @Transient
    RequestFactory requestFactory;

    @Inject
    @Transient
    Injector injector;

    @Override
    public User.Request apply(Map<String, Object> request) {
        final MongoRequest req = (MongoRequest) requestFactory.create(this, request);
        req.createdAt = new DateTime();
        req.status(Request.Status.PENDING);
        datastore.save(req);

        final MongoRequest createdRequest = datastore.get(MongoRequest.class, new ObjectId(req.uuid()));
        enhance(createdRequest);

        if (createdRequest.autoApprovable()) {
            createdRequest.approve(new HashMap<>());
        }

        return createdRequest;
    }

    @Override
    public Optional<User.Request> request(String requestUuid) {
        final MongoRequest request = datastore.get(MongoRequest.class, new ObjectId(requestUuid));

        if (request == null) {
            return Optional.empty();
        }

        if (!request.user.uuid().equals(uuid())) {
            return Optional.empty();
        }

        enhance(request);
        return Optional.of(request);
    }

    private void enhance(MongoRequest request) {
        request.user = this;
        injector.injectMembers(request);
    }
}
