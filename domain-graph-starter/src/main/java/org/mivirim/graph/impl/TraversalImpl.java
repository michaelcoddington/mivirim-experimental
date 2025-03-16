package org.mivirim.graph.impl;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.mivirim.graph.Traversal;

public class TraversalImpl implements Traversal {

    private GraphTraversalSource traversalSource;

    public TraversalImpl(GraphTraversalSource source) {
        this.traversalSource = source;
    }



}
