package org.mivirim.graph.schema;

import java.util.Set;

public class RelationshipDefinition extends Schema {

    private Set<PropertyGroupDefinition> propertyGroupDefinitions;

    private Set<PropertyDefinition> properties;

    public Set<PropertyGroupDefinition> getPropertyGroups() {
        return propertyGroupDefinitions;
    }

    public void setPropertyGroups(Set<PropertyGroupDefinition> propertyGroupDefinitions) {
        this.propertyGroupDefinitions = propertyGroupDefinitions;
    }

    public Set<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropertyDefinition> properties) {
        this.properties = properties;
    }

}
