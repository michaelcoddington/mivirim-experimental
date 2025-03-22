package org.mivirim.graph.schema;

import java.util.Objects;

public abstract class Schema {

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema schema)) return false;
        return Objects.equals(name, schema.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
