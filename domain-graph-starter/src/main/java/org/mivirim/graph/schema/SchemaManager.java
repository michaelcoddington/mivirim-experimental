package org.mivirim.graph.schema;

import java.util.Optional;
import java.util.Set;

public interface SchemaManager {

    Set<EntityDefinition> retrieveEntitySchemas();
    void createEntitySchema(EntityDefinition schema);
    void updateEntitySchema(EntityDefinition schema);
    Optional<EntityDefinition> retrieveEntitySchema(String name);
    void deleteEntitySchema(EntityDefinition schema);

    Set<RelationshipDefinition> retrieveRelationshipSchemas();
    void createRelationshipSchema(RelationshipDefinition schema);
    void updateRelationshipSchema(RelationshipDefinition schema);
    Optional<RelationshipDefinition> retrieveRelationshipSchema(String name);
    void deleteRelationshipSchema(RelationshipDefinition schema);

}
