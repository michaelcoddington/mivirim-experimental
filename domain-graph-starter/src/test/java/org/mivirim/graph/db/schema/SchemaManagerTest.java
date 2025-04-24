package org.mivirim.graph.db.schema;

import com.hazelcast.map.IMap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.JanusAutoConfiguration;
import org.mivirim.graph.cluster.ClusterService;
import org.mivirim.graph.db.schema.impl.SchemaManagerImpl;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SchemaManagerTest {

    private GraphTraversalSource traversalSource;
    private SchemaManager schemaManager;

    @BeforeEach
    void setup() {
        JanusAutoConfiguration janusAutoConfiguration = new JanusAutoConfiguration();
        JanusGraph janusGraph = janusAutoConfiguration.inMemoryGraph();
        traversalSource = janusAutoConfiguration.traversalSource(janusGraph);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);
    }

    @Test
    @DisplayName("create and retrieve entity schema")
    void testCreateEntityDefinition() {
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
        schemaManager.createEntityDefinition(entityDefinition);

        Set<EntityDefinition> definitions = schemaManager.retrieveEntityDefinitions();
        assertEquals(1, definitions.size(), "Incorrect number of entity definitions");

        Optional<EntityDefinition> schemaOpt = schemaManager.retrieveEntityDefinition("Product");
        assertTrue(schemaOpt.isPresent(), "Schema not returned");
        assertEquals(entityDefinition, schemaOpt.get());
    }

}
