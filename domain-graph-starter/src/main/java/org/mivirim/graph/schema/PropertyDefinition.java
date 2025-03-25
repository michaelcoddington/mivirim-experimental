package org.mivirim.graph.schema;

import java.util.Objects;
import java.util.StringJoiner;

public class PropertyDefinition {

    String name;

    PropertyType type;

    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyDefinition propertyDefinition)) return false;
        return Objects.equals(name, propertyDefinition.name) && type == propertyDefinition.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PropertyDefinition.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("type=" + type)
                .toString();
    }
}
