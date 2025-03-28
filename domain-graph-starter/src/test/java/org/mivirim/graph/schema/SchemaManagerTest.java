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
import static org.mockito.Mockito.mock;

public class SchemaManagerTest {

    private GraphTraversalSource traversalSource;
    private SchemaManager schemaManager;

    @BeforeEach
    void setup() {
        JanusAutoConfiguration janusAutoConfiguration = new JanusAutoConfiguration();
        traversalSource = janusAutoConfiguration.traversalSource(janusAutoConfiguration.inMemoryGraph());
        SchemaChangeCoordinator coordinator = mock(SchemaChangeCoordinator.class);
        schemaManager = new SchemaManagerImpl(traversalSource, coordinator);
    }

    @Test
    @DisplayName("create and retrieve entity schema")
    void testCreateEntitySchema() {
        EntityDefinition entityDefinition = new EntityDefinition();
        entityDefinition.setName("Product");

        PropertyDefinition isbnPropertyDefinition = new PropertyDefinition();
        isbnPropertyDefinition.setName("ISBN");
        isbnPropertyDefinition.setType(PropertyType.STRING);

        PropertyDefinition titlePropertyDefinition = new PropertyDefinition();
        titlePropertyDefinition.setName("Title");
        titlePropertyDefinition.setType(PropertyType.STRING);

        PropertyGroupDefinition levelGroupDefinition = new PropertyGroupDefinition();
        levelGroupDefinition.setName("Levels");

        PropertyDefinition graLevelDefinition = new PropertyDefinition();
        graLevelDefinition.setName("GRA");
        graLevelDefinition.setType(PropertyType.STRING);
        levelGroupDefinition.setProperties(Set.of(graLevelDefinition));
        entityDefinition.setPropertyGroups(Set.of(levelGroupDefinition));

        entityDefinition.setProperties(Set.of(isbnPropertyDefinition, titlePropertyDefinition));
        schemaManager.createEntitySchema(entityDefinition);

        Optional<EntityDefinition> schemaOpt = schemaManager.retrieveEntitySchema("Product");
        assertTrue(schemaOpt.isPresent(), "Schema not returned");
        assertEquals(entityDefinition, schemaOpt.get());
    }

}
