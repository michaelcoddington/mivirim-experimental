package org.mivirim.graph.schema.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivirim.graph.DuplicateException;
import org.mivirim.graph.schema.SchemaManager;
import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.RelationshipSchema;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.mivirim.graph.LabelConstants.SCHEMA_LABEL;

@Service
public class SchemaManagerImpl implements SchemaManager {

    private static final Logger LOG = LogManager.getLogger(SchemaManagerImpl.class);

    private GraphTraversalSource traversalSource;

    public SchemaManagerImpl(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    @Override
    public Set<EntitySchema> retrieveEntitySchemas() {
        EntitySchema coverSchema = new EntitySchema();
        coverSchema.setName("Cover");
        return Set.of(coverSchema);
    }

    @Override
    public void createEntitySchema(EntitySchema schema) {
        var tx = traversalSource.tx();
        try {
            traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", schema.getName()).next();
            throw new DuplicateException(String.format("Schema %s already exists", schema.getName()));
        } catch (NoSuchElementException er) {
            traversalSource.addV(SCHEMA_LABEL)
                    .property("name", schema.getName())
                    .next();
            LOG.info("Created schema {}", schema);
        } finally {
            tx.commit();
            tx.close();
        }
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
    public Set<RelationshipSchema> retrieveRelationshipSchemas() {
        return Set.of();
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
