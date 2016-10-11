package com.github.ucluster.mongo.dsl;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("dsl")
public class MongoDSLScript {
    @Id
    protected ObjectId uuid;

    @Property
    protected String model;

    @Property
    protected String type;

    @Property
    protected String script;

    MongoDSLScript() {
    }

    public MongoDSLScript(String model, String type, String script) {
        this.model = model;
        this.type = type;
        this.script = script;
    }

    public String type() {
        return type;
    }

    public String model() {
        return model;
    }

    public String script() {
        return script;
    }
}
