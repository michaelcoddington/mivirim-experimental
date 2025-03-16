package org.mivirim.graph.impl;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivirim.graph.SchemaManager;
import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.RelationshipSchema;

import java.util.Optional;

public class SchemaManagerImpl implements SchemaManager {

    private GraphTraversalSource traversalSource;

    public SchemaManagerImpl(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    @Override
    public void createEntitySchema(EntitySchema schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public void updateEntitySchema(EntitySchema schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<EntitySchema> retrieveEntitySchema(String name) {
        throw new RuntimeException("not done");
    }

    @Override
    public void deleteEntitySchema(EntitySchema schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public void createRelationshipSchema(RelationshipSchema schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public void updateRelationshipSchema(RelationshipSchema schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<RelationshipSchema> retrieveRelationshipSchema(String name) {
        throw new RuntimeException("not done");
    }

    @Override
    public void deleteRelationshipSchema(RelationshipSchema schema) {
        throw new RuntimeException("not done");
    }
}
