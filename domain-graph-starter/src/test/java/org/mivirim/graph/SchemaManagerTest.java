package org.mivirim.graph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.config.JanusConfiguration;
import org.mivirim.graph.impl.SchemaManagerImpl;
import org.mivirim.graph.schema.EntitySchema;
import org.mivirim.graph.schema.StringProperty;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaManagerTest {

    private GraphTraversalSource traversalSource;
    private SchemaManager schemaManager;

    @BeforeEach
    void setup() {
        JanusConfiguration janusConfiguration = new JanusConfiguration();
        traversalSource = janusConfiguration.traversalSource(janusConfiguration.inMemoryGraph());
        schemaManager = new SchemaManagerImpl(traversalSource);
    }

    @Test
    @DisplayName("create and retrieve entity schema")
    void testCreateEntitySchema() {
        EntitySchema entitySchema = new EntitySchema();
        entitySchema.setName("Product");
        StringProperty isbnProperty = new StringProperty();
        isbnProperty.setName("ISBN");
        entitySchema.setProperties(Set.of(isbnProperty));
        schemaManager.createEntitySchema(entitySchema);

        Optional<EntitySchema> schemaOpt = schemaManager.retrieveEntitySchema("Product");
        assertTrue(schemaOpt.isPresent(), "Schema not returned");
        assertEquals(entitySchema, schemaOpt.get());
    }

}
