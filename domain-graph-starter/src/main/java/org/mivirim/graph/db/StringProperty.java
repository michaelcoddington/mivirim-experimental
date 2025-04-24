package org.mivirim.graph.db;

public class StringProperty implements Property<String> {

    private String value;

    private String name;

    public StringProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

}
