package org.mivirim.graph.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivirim.graph.GraphManager;
import org.springframework.stereotype.Component;

@Component
public class GraphManagerImpl implements GraphManager {

    private static final Logger LOG = LogManager.getLogger(GraphManagerImpl.class);

    private GraphTraversalSource traversalSource;

    public GraphManagerImpl(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    @Override
    public void resetGraph() {
        LOG.warn("Resetting graph. Dropping all vertices and edges.");
        var transaction = traversalSource.tx();
        transaction.begin();
        traversalSource.V().drop();
        traversalSource.E().drop();
        transaction.commit();
        transaction.close();
    }

}
