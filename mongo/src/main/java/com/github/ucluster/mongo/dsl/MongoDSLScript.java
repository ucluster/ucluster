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
    protected String userType;

    @Property
    protected String scriptType;

    @Property
    protected String script;

    MongoDSLScript() {
    }

    public MongoDSLScript(String userType, String scriptType, String script) {
        this.userType = userType;
        this.scriptType = scriptType;
        this.script = script;
    }

    public MongoDSLScript(String scriptType, String script) {
        this("default", scriptType, script);
    }

    public String type() {
        return userType;
    }

    public String script() {
        return script;
    }
}
