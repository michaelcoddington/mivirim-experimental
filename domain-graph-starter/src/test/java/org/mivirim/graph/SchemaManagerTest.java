package org.mivirim.graph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.config.JanusConfiguration;
import org.mivirim.graph.impl.SchemaManagerImpl;

import static org.junit.jupiter.api.Assertions.fail;

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
    @DisplayName("create entity schema")
    void testCreateEntitySchema() {
        fail("not done");
    }

}
