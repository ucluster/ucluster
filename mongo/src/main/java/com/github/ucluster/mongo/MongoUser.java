package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
import com.github.ucluster.core.RequestFactory;
import com.github.ucluster.core.User;
import com.github.ucluster.core.util.Criteria;
import com.github.ucluster.core.util.PaginatedList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.USERS;

@Entity(USERS)
public class MongoUser extends MongoRecord<User> implements User, Model {

    @Inject
    @Transient
    RequestFactory requestFactory;

    @Override
    public User.Request apply(Map<String, Object> request) {
        final MongoRequest req = saveRequest(request);

        if (req.auto()) {
            req.approve(request);
        }

        return req;
    }

    @Override
    public Optional<User.Request> request(String requestUuid) {
        try {
            final MongoRequest request = datastore.get(MongoRequest.class, new ObjectId(requestUuid));

            if (request == null || !request.user.uuid().equals(uuid())) {
                return Optional.empty();
            }

            enhance(request);
            return Optional.of(request);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public PaginatedList<Request> requests(Criteria criteria) {
        final Query<MongoRequest> query = datastore.createQuery(MongoRequest.class)
                .disableValidation()
                .field("user").equal(new Key<>(MongoUser.class, USERS, uuid));

        criteria.params(e -> {
            query.field(MongoProperty.valueMongoField(e.getKey())).in(e.getValue());
        });

        return new PaginatedList<>(query.countAll(),
                (page, perPage) -> query
                        .order("-createdAt")
                        .offset((page - 1) * perPage)
                        .limit(perPage)
                        .asList());
    }

    private void enhance(MongoRequest request) {
        request.user = this;
        injector.injectMembers(request);
    }

    private MongoRequest saveRequest(Map<String, Object> request) {
        final MongoRequest req = (MongoRequest) requestFactory.create(this, request);
        enhance(req);
        req.save();
        return req;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = super.toJson();
        json.put("uri", Routing.user(this));

        return json;
    }
}
