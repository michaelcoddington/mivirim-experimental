package org.mivirim.graph.db;

import java.util.HashSet;
import java.util.Set;

public class EntityMutation {

    private String entityType;
    private Set<Property> properties = new HashSet<>();

    public EntityMutation entityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public EntityMutation stringProperty(String name, String value) {
        properties.add(new StringProperty(name, value));
        return this;
    }

    public EntityMutation intProperty(String name, int value) {
        properties.add(new IntProperty(name, value));
        return this;
    }

    public Set<Property> getProperties() {
        return properties;
    }

}

