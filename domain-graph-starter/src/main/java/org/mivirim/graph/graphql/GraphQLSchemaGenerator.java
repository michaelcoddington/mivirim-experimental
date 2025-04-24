package org.mivirim.graph.graphql;

import graphql.schema.idl.TypeDefinitionRegistry;
import org.mivirim.graph.db.schema.EntityDefinition;
import org.mivirim.graph.db.schema.RelationshipDefinition;

import java.util.Set;

public interface GraphQLSchemaGenerator {

    TypeDefinitionRegistry generateTypeDefinitions(Set<EntityDefinition> entityDefinitions, Set<RelationshipDefinition> relationshipDefinitions);

}
