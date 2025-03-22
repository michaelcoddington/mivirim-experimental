package org.mivirim.graph.schema.dto;

import java.util.Set;

public class SchemaDto {

    private String name;
    private Set<SchemaPropertyGroupDto> propertyGroups;
    private Set<SchemaPropertyDto> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SchemaPropertyGroupDto> getPropertyGroups() {
        return propertyGroups;
    }

    public void setPropertyGroups(Set<SchemaPropertyGroupDto> propertyGroups) {
        this.propertyGroups = propertyGroups;
    }

    public Set<SchemaPropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(Set<SchemaPropertyDto> properties) {
        this.properties = properties;
    }

}
