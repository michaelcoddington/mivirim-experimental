package org.mivirim.graph.schema;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.JanusAutoConfiguration;
import org.mivirim.graph.schema.impl.SchemaManagerImpl;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaManagerTest {

    private GraphTraversalSource traversalSource;
    private SchemaManager schemaManager;

    @BeforeEach
    void setup() {
        JanusAutoConfiguration janusAutoConfiguration = new JanusAutoConfiguration();
        traversalSource = janusAutoConfiguration.traversalSource(janusAutoConfiguration.inMemoryGraph());
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
