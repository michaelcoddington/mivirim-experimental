package org.mivirim.graph.graphql.impl;

import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.mivirim.graph.graphql.GraphQLSchemaGenerator;
import org.mivirim.graph.schema.EntityDefinition;
import org.mivirim.graph.schema.RelationshipDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GraphQLSchemaGeneratorImpl implements GraphQLSchemaGenerator {

    @Override
    public TypeDefinitionRegistry generateTypeDefinitions(Set<EntityDefinition> entityDefinitions, Set<RelationshipDefinition> relationshipDefinitions) {
        TypeDefinitionRegistry registry = new TypeDefinitionRegistry();

        for (EntityDefinition entityDefinition: entityDefinitions) {
            registry.add(getEntityTypeDefinition(entityDefinition));
        }

        return registry;
    }

    private ObjectTypeDefinition getEntityTypeDefinition(EntityDefinition entityDefinition) {
        Description entityDescription = entityDefinition.getDescription() == null ? null : new Description(entityDefinition.getDescription(), null, false);
        List<FieldDefinition> fieldDefinitions = entityDefinition.getProperties().stream()
                .map(propertyDefinition -> {
                    TypeName typeName = switch (propertyDefinition.getType()) {
                        case STRING -> new TypeName("String");
                        default -> throw new RuntimeException("Unsupported type: " + propertyDefinition.getType());
                    };
                    Description propertyDescription = propertyDefinition.getDescription() == null ? null : new Description(propertyDefinition.getDescription(), null, false);

                    return FieldDefinition.newFieldDefinition()
                            .name(propertyDefinition.getName())
                            .type(typeName)
                            .description(propertyDescription)
                            .build();
                }).toList();
        return ObjectTypeDefinition.newObjectTypeDefinition()
                .name(entityDefinition.getName())
                .description(entityDescription)
                .fieldDefinitions(fieldDefinitions)
                .build();
    }

}
