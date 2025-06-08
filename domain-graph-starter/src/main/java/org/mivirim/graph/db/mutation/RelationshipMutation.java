package org.mivirim.graph.db.mutation;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelationshipMutation {

    /*
    A relationship mutation will only contain a source and target when being created.
    When updating or deleting a relationship, we use a relationship id.
     */

    private Object id;
    private String relationshipType;
    private EntityReference sourceReference;
    private EntityReference targetReference;
    private Map<String, Property> properties = new HashMap<>();

    public RelationshipMutation relationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
        return this;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public RelationshipMutation id(Object id) {
        this.id = id;
        return this;
    }

    public Object getId() {
        return id;
    }

    public RelationshipMutation source(EntityReference source) {
        this.sourceReference = source;
        return this;
    }

    public EntityReference getSource() {
        return sourceReference;
    }

    public RelationshipMutation target(EntityReference target) {
        this.targetReference = target;
        return this;
    }

    public EntityReference getTarget() {
        return targetReference;
    }

    public RelationshipMutation stringProperty(String name, String value) {
        properties.put(name, new StringProperty(name, value));
        return this;
    }

    public RelationshipMutation stringListProperty(String name, List<String> values) {
        properties.put(name, new StringListProperty(name, values));
        return this;
    }

    public RelationshipMutation intProperty(String name, Integer value) {
        properties.put(name, new IntProperty(name, value));
        return this;
    }

    public RelationshipMutation intListProperty(String name, List<Integer> values) {
        properties.put(name, new IntListProperty(name, values));
        return this;
    }

    public RelationshipMutation doubleProperty(String name, double value) {
        properties.put(name, new DoubleProperty(name, value));
        return this;
    }

    public RelationshipMutation doubleListProperty(String name, List<Double> values) {
        properties.put(name, new DoubleListProperty(name, values));
        return this;
    }

    public RelationshipMutation booleanProperty(String name, boolean value) {
        properties.put(name, new BooleanProperty(name, value));
        return this;
    }

    public RelationshipMutation booleanListProperty(String name, List<Boolean> values) {
        properties.put(name, new BooleanListProperty(name, values));
        return this;
    }

    public RelationshipMutation dateProperty(String name, Date value) {
        properties.put(name, new DateProperty(name, value));
        return this;
    }

    public RelationshipMutation dateListProperty(String name, List<Date> values) {
        properties.put(name, new DateListProperty(name, values));
        return this;
    }

    public RelationshipMutation binaryReferenceProperty(String name, Long nodeId) {
        properties.put(name, new BinaryReferenceProperty(name, nodeId));
        return this;
    }

    public Set<Property> getProperties() {
        return new HashSet<>(properties.values());
    }

}
