package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity("users")
public class MongoUser extends MongoRecord<User> implements User, Model {

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
            createdRequest.approve(request);
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

    @Override
    public PaginatedList<Request> requests(Criteria criteria) {
        final Query<MongoRequest> query = datastore.createQuery(MongoRequest.class).disableValidation();

        criteria.params(e -> {
            query.field(MongoProperty.valueMongoField(e.getKey())).in(e.getValue());
        });

        return new PaginatedList<>(query.countAll(),
                (page, perPage) -> query
                        .offset((page - 1) * perPage)
                        .limit(perPage)
                        .asList());
    }

    private void enhance(MongoRequest request) {
        request.user = this;
        injector.injectMembers(request);
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("id", uuid());
        json.put("uri", Routing.user(this));
        json.put("created_at", createdAt());
        json.put("metadata", metadata());
        json.put("properties", properties().stream()
                .filter(prop -> !definition().property(prop.path()).definition().containsKey("credential"))
                .collect(Collectors.toMap(Property::path, Property::value)));

        return json;
    }

    @Override
    public Map<String, Object> toReferenceJson() {
        return toJson();
    }
}
