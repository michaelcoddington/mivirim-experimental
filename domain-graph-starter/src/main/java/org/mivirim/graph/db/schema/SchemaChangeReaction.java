package org.mivirim.graph.db.schema;

@FunctionalInterface
public interface SchemaChangeReaction {

    void onSchemaChange();

}
