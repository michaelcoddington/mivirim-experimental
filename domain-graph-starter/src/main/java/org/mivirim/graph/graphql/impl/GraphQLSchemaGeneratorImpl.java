package org.mivirim.graph.graphql.impl;

import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.mivirim.graph.graphql.GraphQLSchemaGenerator;
import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.RelationshipSchema;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GraphQLSchemaGeneratorImpl implements GraphQLSchemaGenerator {

    @Override
    public TypeDefinitionRegistry generateTypeDefinitions(Set<EntitySchema> entitySchemas, Set<RelationshipSchema> relationshipSchemas) {
        TypeDefinitionRegistry registry = new TypeDefinitionRegistry();

        for (EntitySchema schema: entitySchemas) {
            ObjectTypeDefinition entityTypeDefinition = ObjectTypeDefinition.newObjectTypeDefinition()
                    .name(schema.getName())
                    .fieldDefinitions(List.of(
                                    FieldDefinition.newFieldDefinition()
                                            .name("name")
                                            .type(new TypeName("String"))
                                            .description(new Description("Some name", null, false))
                                            .build()
                            ))
                    .build();
            registry.add(entityTypeDefinition);
        }

        return registry;
    }

}
