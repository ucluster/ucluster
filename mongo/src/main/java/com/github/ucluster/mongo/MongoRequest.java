package com.github.ucluster.mongo;

import com.github.ucluster.api.Routing;
import com.github.ucluster.core.ApiRequest;
import com.github.ucluster.core.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.github.ucluster.mongo.Constants.Collection.REQUESTS;

@Entity(REQUESTS)
public class MongoRequest extends MongoRecord<User.Request> implements User.Request, Model {
    @Reference
    protected User user;

    @Transient
    protected User.Request.Response response;

    protected MongoRequest() {
    }

    public MongoRequest(User user, ApiRequest request) {
        this.user = user;
        request.model(Constants.Record.REQUEST);
        this.metadata = request.metadata();
        loadProperties(request);
    }

    private void loadProperties(ApiRequest request) {
        request.properties().entrySet().stream()
                .forEach(e -> {
                    property(e.getKey(), e.getValue());
                });
    }

    @Override
    public String type() {
        return metadata.get("type");
    }

    @Override
    public Optional<User.Request.Response> response() {
        return Optional.ofNullable(response);
    }

    @Override
    public void execute(ApiRequest request) {
        throw new RuntimeException("need implemented");
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = super.toJson();
        json.put("uri", Routing.request(user, this));

        return json;
    }

    @Override
    public Map<String, Object> toReferenceJson() {
        return toJson();
    }

    public static class Response implements User.Request.Response {
        private Collection<User.Request.Response.Attribute> attributes = new ArrayList<>();

        Response() {
        }

        Response(Collection<User.Request.Response.Attribute> attributes) {
            this.attributes = attributes;
        }

        public static User.Request.Response empty() {
            return new Response();
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public Collection<User.Request.Response.Attribute> attributes() {
            return attributes;
        }

        public static class Builder {
            private Collection<User.Request.Response.Attribute> attributes = new ArrayList<>();
            private String key;

            public Builder key(String key) {
                this.key = key;
                return this;
            }

            public Builder value(String value) {
                attributes.add(new Attribute(key, value));
                return this;
            }

            public User.Request.Response get() {
                return new Response(attributes);
            }
        }

        public static class Attribute implements User.Request.Response.Attribute {
            private final String key;
            private final String value;

            public Attribute(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public String value() {
                return value;
            }
        }
    }
}
