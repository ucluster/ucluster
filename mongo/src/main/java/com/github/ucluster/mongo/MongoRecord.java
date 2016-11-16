package com.github.ucluster.mongo;

import com.github.ucluster.core.Record;
import com.github.ucluster.core.definition.Definition;
import com.github.ucluster.core.definition.DefinitionRepository;
import com.github.ucluster.core.exception.RecordException;
import com.google.inject.Injector;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MongoRecord<T extends Record> implements Record, Model {
    @Id
    protected ObjectId uuid;

    @Version
    protected Long version;

    @org.mongodb.morphia.annotations.Property
    protected final DateTime createdAt = new DateTime();

    @Embedded
    protected Map<String, String> metadata = new HashMap<>();

    @Embedded
    protected Map<String, Property> properties = new HashMap<>();

    @Inject
    @Transient
    protected DefinitionRepository<Definition<T>> definitions;

    @Inject
    @Transient
    protected Datastore datastore;

    @Inject
    @Transient
    protected Injector injector;

    @Override
    public String uuid() {
        return uuid.toHexString();
    }

    @Override
    public DateTime createdAt() {
        return createdAt;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public void property(Property property) {
        recordDirtyProperty(property);
        properties.put(property.path(), property);
    }

    @Override
    public <V> void property(String path, V value) {
        property(new MongoProperty<>(path, value));
    }

    @Override
    public Optional<Property> property(String path) {
        return Optional.ofNullable(properties.get(path));
    }

    @Override
    public Collection<Property> properties() {
        return properties.values();
    }

    /**
     * save:
     * Called when you want to `CREATE` this record. All the properties defined in dsl will be passed through to the concerns
     */
    @Override
    public void save() {
        validateSave();
        beforeSaveOn(Record.Property.Point.BEFORE_CREATE);
        doSave();
        afterSave();
    }

    /**
     * update:
     * Called when you want to `UPDATE` this record with some properties. The properties updated will be passed through to the concerns
     */
    @Override
    public void update() {
        validateUpdate();
        beforeSaveOn(Record.Property.Point.BEFORE_UPDATE);
        doUpdate();
        afterSave();
    }

    private void validateSave() {
        definition().effect(
                Record.Property.Point.VALIDATE,
                (T) this);
    }

    private void validateUpdate() {
        definition().effect(
                Record.Property.Point.VALIDATE,
                (T) this,
                dirtyTracker.asArray());
    }

    private void beforeSaveOn(Record.Property.Point point) {
        definition().effect(point, (T) this, dirtyTracker.asArray());
    }

    private void doSave() {
        filterNullValueProperty();
        datastore.save(this);
    }

    private void filterNullValueProperty() {
        properties = properties.entrySet().stream()
                .filter(e -> e.getValue().value() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void doUpdate() {
        final UpdateResults results = datastore.update(this, generateDirtyUpdateOperations(this));
        if (!results.getUpdatedExisting()) {
            throw new RecordException("updated.not.effect");
        }
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

    protected Map<String, Object> deliveryProperties() {
        definition().effect(Property.Point.DELIVERY, (T) this);
        return properties().stream()
                .filter(property -> property.value() != null)
                .collect(Collectors.toMap(Property::path, Property::value));
    }

    @Transient
    protected DirtyTracker dirtyTracker = new DirtyTracker();

    protected void recordDirtyProperty(Record.Property property) {
        dirtyTracker.dirty(property.path());
    }

    public Definition<T> definition() {
        return definitions.find(metadata());
    }

    @Override
    public Optional<String> metadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    private static class DirtyTracker {
        Set<String> dirtyProperties = new HashSet<>();

        void dirty(String propertyPath) {
            dirtyProperties.add(propertyPath);
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

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("id", uuid());
        json.put("created_at", createdAt());
        json.put("metadata", metadata());
        json.put("properties", deliveryProperties());

        return json;
    }

    @Override
    public Map<String, Object> toReferenceJson() {
        return toJson();
    }
}
