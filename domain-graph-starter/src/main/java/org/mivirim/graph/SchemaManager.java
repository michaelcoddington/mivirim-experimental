package org.mivirim.graph;

import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.RelationshipSchema;

import java.util.Optional;

public interface SchemaManager {

    void createEntitySchema(EntitySchema schema);
    void updateEntitySchema(EntitySchema schema);
    Optional<EntitySchema> retrieveEntitySchema(String name);
    void deleteEntitySchema(EntitySchema schema);

    void createRelationshipSchema(RelationshipSchema schema);
    void updateRelationshipSchema(RelationshipSchema schema);
    Optional<RelationshipSchema> retrieveRelationshipSchema(String name);
    void deleteRelationshipSchema(RelationshipSchema schema);

}
