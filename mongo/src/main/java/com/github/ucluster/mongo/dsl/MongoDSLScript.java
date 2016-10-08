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
    protected String recordType;

    @Property
    protected String recordGroup;

    @Property
    protected String script;

    MongoDSLScript() {
    }

    public MongoDSLScript(String recordType, String recordGroup, String script) {
        this.recordType = recordType;
        this.recordGroup = recordGroup;
        this.script = script;
    }

    public String getRecordType() {
        return recordType;
    }

    public String type() {
        return recordGroup;
    }

    public String script() {
        return script;
    }
}
