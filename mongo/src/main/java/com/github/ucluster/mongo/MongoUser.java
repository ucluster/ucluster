package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.User;
import com.github.ucluster.mongo.converter.JodaDateTimeConverter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity("users")
@Converters(JodaDateTimeConverter.class)
public class MongoUser extends MongoRecord<User> implements User {

    @Override
    public User.Request apply(Map<String, Object> request) {
        final UserUpdateNicknameRequest req = new UserUpdateNicknameRequest(request);
        req.user = this;

        datastore.save(req);

        final MongoRequest createdRequest = datastore.get(MongoRequest.class, new ObjectId(req.uuid()));
        createdRequest.user = this;
        createdRequest.approve(new HashMap<>());

        datastore.update(createdRequest, datastore.createUpdateOperations(Record.class)
                .disableValidation().set("status", createdRequest.status()));

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

        return Optional.of(request);
    }
}
