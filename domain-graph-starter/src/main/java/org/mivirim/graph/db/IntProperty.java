package org.mivirim.graph.db;

public class IntProperty implements Property<Integer> {

    private String name;
    private Integer value;

    public IntProperty(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
