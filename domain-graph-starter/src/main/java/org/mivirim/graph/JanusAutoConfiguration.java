package org.mivirim.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JanusAutoConfiguration {

    private static final Logger LOG = LogManager.getLogger(JanusAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(value = "graph.backend", havingValue = "berkeley")
    public JanusGraph berkeleyGraph() {
        LOG.info("Initializing Berkeley graph");
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
        LOG.info("Initializing Cassandra graph");
        return JanusGraphFactory.build().
                set("storage.backend", "cql")
                .set("storage.hostname", "localhost")
                .open();
    }

    @Bean
    @ConditionalOnMissingBean(JanusGraph.class)
    public JanusGraph inMemoryGraph() {
        LOG.info("Initializing in-memory graph");
        return JanusGraphFactory.build()
                .set("storage.backend", "inmemory")
                .set("index.search.backend", "lucene")
                .set("index.search.directory", "db/lucene")
                .open();
    }

    @Bean
    public GraphTraversalSource traversalSource(JanusGraph graph) {
        return graph.traversal();
    }

    /*
    @Bean
    public JanusGraphIndex index(JanusGraph graph) throws InterruptedException{
        LOG.info("Initializating JanusGraph index");
        JanusGraphManagement management = graph.openManagement();
        management.makePropertyKey("name").dataType(String.class).make();
        management.makePropertyKey("age").dataType(Integer.class).make();
        JanusGraphIndex index = management.buildIndex("testIndex", Vertex.class)
                .addKey(management.getPropertyKey("name"))
                .addKey(management.getPropertyKey("age"))
                .buildMixedIndex("search");
        management.commit();


        GraphIndexStatusReport report = ManagementSystem.awaitGraphIndexStatus(graph, "testIndex").status(SchemaStatus.ENABLED).call();
        LOG.info("Index status: {}", report);

        management = graph.openManagement();
        LOG.info(management.printSchema());
        LOG.info(management.printIndexes());


        return index;
    }

     */

}
