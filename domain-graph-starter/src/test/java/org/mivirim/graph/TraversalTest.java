package org.mivirim.graph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.config.JanusConfiguration;
import org.mivirim.graph.impl.GraphManagerImpl;

import static org.junit.jupiter.api.Assertions.fail;

public class TraversalTest {

    private GraphTraversalSource traversalSource;
    private GraphManager graphManager;

    @BeforeEach
    void setup() {
        JanusConfiguration janusConfiguration = new JanusConfiguration();
        traversalSource = janusConfiguration.traversalSource(janusConfiguration.inMemoryGraph());
        graphManager = new GraphManagerImpl(traversalSource);
        graphManager.resetGraph();
    }

    @Test
    @DisplayName("create vertex")
    void testCreateVertex() {
        fail("not done");
    }

}
