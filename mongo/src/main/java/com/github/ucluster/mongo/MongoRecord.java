package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MongoRecord<T extends Record> implements Record {
    @Id
    protected ObjectId uuid;

    @org.mongodb.morphia.annotations.Property
    protected DateTime createdAt;

    @Embedded
    protected Map<String, Object> metadata = new HashMap<>();

    @Embedded
    protected Map<String, Property> properties = new HashMap<>();

    @Transient
    protected Definition<T> definition;

    @Inject
    @Transient
    protected Datastore datastore;

    @Override
    public String uuid() {
        return uuid.toHexString();
    }

    @Override
    public DateTime createdAt() {
        return createdAt;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public void property(Property property) {
        recordDirtyProperty(property);
        properties.put(property.path(), property);
    }

    @Override
    public Optional<Property> property(String propertyPath) {
        return Optional.ofNullable(properties.get(propertyPath));
    }

    @Override
    public Collection<Property> properties() {
        return properties.values();
    }

    @Override
    public void save() {
        validate();
        beforeSaveOn(Record.Property.Point.BEFORE_CREATE);
        doSave();
        afterSave();
    }

    @Override
    public void update() {
        validate();
        beforeSaveOn(Record.Property.Point.BEFORE_UPDATE);
        doUpdate();
        afterSave();
    }

    private void validate() {
        definition.effect(Record.Property.Point.VALIDATE, (T) this, dirtyTracker.asArray());
    }

    private void beforeSaveOn(Record.Property.Point point) {
        definition.effect(point, (T) this, dirtyTracker.asArray());
    }

    private void doSave() {
        datastore.save(this);
    }

    private void doUpdate() {
        datastore.update(this, generateDirtyUpdateOperations(this));
    }

    private UpdateOperations<Record> generateDirtyUpdateOperations(Record record) {
        final UpdateOperations<Record> operations = datastore.createUpdateOperations(Record.class)
                .disableValidation();

        dirtyTracker.dirties().stream()
                .map(record::property)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(property ->
                        operations.set(MongoProperty.mongoField(property), property)
                );

        return operations;
    }

    private void afterSave() {
        dirtyTracker.clear();
    }

    @Transient
    protected DirtyTracker dirtyTracker = new DirtyTracker();

    protected void recordDirtyProperty(Record.Property property) {
        dirtyTracker.dirty(property.path());
    }

    private static class DirtyTracker {
        Set<String> dirtyProperties = new HashSet<>();

        void dirty(String propertyPath) {
            dirtyProperties.add(propertyPath);
        }

        boolean isDirty(String propertyPath) {
            return dirtyProperties.contains(propertyPath);
        }

        Collection<String> dirties() {
            return dirtyProperties;
        }

        String[] asArray() {
            return dirtyProperties.toArray(new String[dirtyProperties.size()]);
        }

        void clear() {
            dirtyProperties.clear();
        }
    }
}
