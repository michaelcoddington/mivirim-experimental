package org.mivirim.graph.schema;

import java.util.Optional;
import java.util.Set;

public interface SchemaManager {

    Set<EntitySchema> retrieveEntitySchemas();
    void createEntitySchema(EntitySchema schema);
    void updateEntitySchema(EntitySchema schema);
    Optional<EntitySchema> retrieveEntitySchema(String name);
    void deleteEntitySchema(EntitySchema schema);

    Set<RelationshipSchema> retrieveRelationshipSchemas();
    void createRelationshipSchema(RelationshipSchema schema);
    void updateRelationshipSchema(RelationshipSchema schema);
    Optional<RelationshipSchema> retrieveRelationshipSchema(String name);
    void deleteRelationshipSchema(RelationshipSchema schema);

}
