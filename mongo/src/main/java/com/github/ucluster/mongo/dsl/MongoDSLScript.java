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
    private String type;

    @Property
    protected String script;

    MongoDSLScript() {
    }

    //more attribute comes later for selection
    public MongoDSLScript(String type, String script) {
        this.type = type;
        this.script = script;
    }

    public String type() {
        return type;
    }

    public String script() {
        return script;
    }
}
