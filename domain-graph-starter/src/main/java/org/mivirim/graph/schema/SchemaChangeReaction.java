package org.mivirim.graph.schema;

@FunctionalInterface
public interface SchemaChangeReaction {

    void onSchemaChange();

}
