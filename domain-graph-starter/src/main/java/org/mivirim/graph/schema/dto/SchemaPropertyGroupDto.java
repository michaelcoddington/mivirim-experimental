package org.mivirim.graph.schema.dto;

import java.util.Set;

public class SchemaPropertyGroupDto {

    private String name;

    private Set<SchemaPropertyDto> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SchemaPropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(Set<SchemaPropertyDto> properties) {
        this.properties = properties;
    }

}
