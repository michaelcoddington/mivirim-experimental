package org.mivirim.graph.config;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JanusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JanusGraph inMemoryGraph() {
        return JanusGraphFactory.build()
                .set("storage.backend", "inmemory")
                .open();
    }

    @Bean
    @ConditionalOnProperty(value = "graph.backend", havingValue = "berkeley")
    public JanusGraph berkeleyGraph() {
        return JanusGraphFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "db/berkeleyje")
                .set("index.search.backend", "lucene")
                .set("index.search.directory", "db/lucene")
                .open();
    }

    @Bean
    @ConditionalOnProperty(value = "graph.backend", havingValue = "cassandra")
    public JanusGraph cassandraGraph() {
        return JanusGraphFactory.build().
                set("storage.backend", "cql")
                .set("storage.hostname", "localhost")
                .open();
    }

    @Bean
    public GraphTraversalSource traversalSource(JanusGraph graph) {
        return graph.traversal();
    }

}
