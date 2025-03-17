package org.mivirim.graph.schema;

public interface SchemaChangeCoordinator {

    void signalEntitySchemaChange();

    void addSchemaChangeReaction(SchemaChangeReaction reaction);

}
