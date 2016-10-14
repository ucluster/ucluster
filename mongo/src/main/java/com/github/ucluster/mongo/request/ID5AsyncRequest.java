package com.github.ucluster.mongo.request;

import com.github.ucluster.core.User;
import com.github.ucluster.mongo.Model;
import com.github.ucluster.mongo.MongoProperty;
import com.github.ucluster.mongo.MongoRequest;
import com.github.ucluster.mongo.json.JsonRequest;

import java.util.Map;

public class ID5AsyncRequest extends MongoRequest implements Model {
    public ID5AsyncRequest() {
        super();
    }

    public ID5AsyncRequest(User user, Map<String, Object> request) {
        super(user, request);
    }

    @Override
    public boolean auto() {
        return false;
    }

    @Override
    public User.Request.Response approve(Map<String, Object> detail) {
        status(Status.APPROVED);

        user.property(property("id_name").get());
        user.property(property("id_number").get());

        user.update();
        update();

        return Response.empty();
    }

    @Override
    public User.Request.Response reject(Map<String, Object> detail) {
        status(Status.REJECTED, new MongoProperty<>("reason", reason(detail)));
        update();

        return Response.empty();
    }

    private Object reason(Map<String, Object> detail) {
        return JsonRequest.of(detail).property("reason");
    }
}
