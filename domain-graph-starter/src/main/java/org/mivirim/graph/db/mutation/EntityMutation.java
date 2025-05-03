package org.mivirim.graph.db.mutation;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityMutation {

    private String entityType;
    private Object id;
    private Set<Property> properties = new HashSet<>();

    public EntityMutation entityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public EntityMutation id(Object id) {
        this.id = id;
        return this;
    }

    public Object getId() {
        return id;
    }

    public EntityMutation stringProperty(String name, String value) {
        properties.add(new StringProperty(name, value));
        return this;
    }

    public EntityMutation stringListProperty(String name, List<String> values) {
        properties.add(new StringListProperty(name, values));
        return this;
    }

    public EntityMutation intProperty(String name, int value) {
        properties.add(new IntProperty(name, value));
        return this;
    }

    public EntityMutation intListProperty(String name, List<Integer> values) {
        properties.add(new IntListProperty(name, values));
        return this;
    }

    public EntityMutation doubleProperty(String name, double value) {
        properties.add(new DoubleProperty(name, value));
        return this;
    }

    public EntityMutation doubleListProperty(String name, List<Double> values) {
        properties.add(new DoubleListProperty(name, values));
        return this;
    }

    public EntityMutation booleanProperty(String name, boolean value) {
        properties.add(new BooleanProperty(name, value));
        return this;
    }

    public EntityMutation booleanListProperty(String name, List<Boolean> values) {
        properties.add(new BooleanListProperty(name, values));
        return this;
    }

    public EntityMutation dateProperty(String name, Date value) {
        properties.add(new DateProperty(name, value));
        return this;
    }

    public EntityMutation dateListProperty(String name, List<Date> values) {
        properties.add(new DateListProperty(name, values));
        return this;
    }

    public EntityMutation binaryReferenceProperty(String name, Long nodeId) {
        properties.add(new BinaryReferenceProperty(name, nodeId));
        return this;
    }

    public Set<Property> getProperties() {
        return properties;
    }

}

