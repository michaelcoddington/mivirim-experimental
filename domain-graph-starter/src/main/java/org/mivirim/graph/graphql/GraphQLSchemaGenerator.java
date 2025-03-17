package org.mivirim.graph.graphql;

import graphql.schema.idl.TypeDefinitionRegistry;
import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.RelationshipSchema;

import java.util.Set;

public interface GraphQLSchemaGenerator {

    TypeDefinitionRegistry generateTypeDefinitions(Set<EntitySchema> entitySchemas, Set<RelationshipSchema> relationshipSchemas);

}
