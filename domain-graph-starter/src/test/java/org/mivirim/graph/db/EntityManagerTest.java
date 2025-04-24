package org.mivirim.graph.db;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.JanusAutoConfiguration;
import org.mivirim.graph.db.impl.EntityManagerImpl;
import org.mivirim.graph.db.storage.BinaryStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mivirim.graph.LabelConstants.HAS_VERSION_LABEL;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = JanusAutoConfiguration.class)
@DisplayName("An entity manager")
public class EntityManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerTest.class);

    @Autowired
    private GraphTraversalSource traversalSource;

    @BeforeEach
    public void init() {
        try {
            traversalSource.V().drop().next();
        } catch (NoSuchElementException nee) { }
    }

    @Test
    @DisplayName("should return a transaction after executing a mutation")
    public void testReturnTransactionOnMutation() {

        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);

        // test each type
        /*
         * int
         * string
         * double?
         * boolean
         * date
         * binary ref
         * List<int>
         * List<string>
         * List<boolean>
         * List<double>
         * List<date>
         */

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg")
                .intProperty("number", 5);
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction transaction = manager.executeMutation(request);
        assertNotNull(transaction, "null transaction");

        LOG.info("Running example query");
        traversalSource.V().has("name").next();

        LOG.info("Running next query");
        var elementMap = traversalSource.V()
                .hasLabel("Photo")
                .project("elements", "versions")
                .by(__.elementMap())
                .by(__.out(HAS_VERSION_LABEL).elementMap().fold()).next();



        LOG.info(elementMap.toString());
    }

}
