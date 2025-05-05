package org.mivirim.graph.db;

import com.google.common.collect.Streams;
import com.hazelcast.map.IMap;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.cluster.ClusterService;
import org.mivirim.graph.db.impl.EntityManagerImpl;
import org.mivirim.graph.db.mutation.EntityMutation;
import org.mivirim.graph.db.mutation.MutationRequest;
import org.mivirim.graph.db.schema.EntityDefinition;
import org.mivirim.graph.db.schema.PropertyDefinition;
import org.mivirim.graph.db.schema.PropertyType;
import org.mivirim.graph.db.schema.SchemaManager;
import org.mivirim.graph.db.schema.impl.SchemaManagerImpl;
import org.mivirim.graph.db.storage.BinaryStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@DisplayName("An entity manager")
public class EntityManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerTest.class);

    private GraphTraversalSource traversalSource;
    private JanusGraph janusGraph;

    @BeforeEach
    public void init() throws IOException {
        if (janusGraph != null && janusGraph.isOpen()) {
            janusGraph.close();
        }

        String indexPath;
        do {
            String tmpId = UUID.randomUUID().toString().substring(0, 8);
            indexPath = "target/db/lucene-test-" + tmpId;
        } while (new File(indexPath).exists());


        File indexDir = new File(indexPath);
        janusGraph = JanusGraphFactory.build()
                .set("storage.backend", "inmemory")
                .set("index.search.backend", "lucene")
                .set("index.search.directory", indexPath)
                .set("cache.tx-cache-size", 0)
                .open();
        traversalSource = janusGraph.traversal();
        try {
            traversalSource.V().drop().next();
        } catch (NoSuchElementException nee) { }

        janusGraph.tx().close();
    }

    @Test
    @DisplayName("should return a transaction after executing a mutation")
    public void testReturnTransactionOnMutation() {

        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg");
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction transaction = manager.executeMutation(request);
        assertNotNull(transaction, "null transaction");
    }

    private void setUpSchema(SchemaManager schemaManager) {
        EntityDefinition entityDefinition = new EntityDefinition();
        entityDefinition.setName("Photo");

        PropertyDefinition nameDefinition = new PropertyDefinition("name", PropertyType.STRING, "");
        PropertyDefinition numberDefinition = new PropertyDefinition("number", PropertyType.INT, "");
        PropertyDefinition booleanDefinition = new PropertyDefinition("boolean", PropertyType.BOOLEAN, "");
        PropertyDefinition dateDefinition = new PropertyDefinition("someDate", PropertyType.DATE, "");
        PropertyDefinition doublePropertyDefinition = new PropertyDefinition("double", PropertyType.DOUBLE, "");
        PropertyDefinition intListDefinition = new PropertyDefinition("intList", PropertyType.INT_LIST, "");
        entityDefinition.setProperties(Set.of(nameDefinition, numberDefinition, booleanDefinition, dateDefinition, doublePropertyDefinition, intListDefinition));

        schemaManager.createEntityDefinition(entityDefinition);
    }

    @Test
    @DisplayName("should mark new entities as uncommitted")
    void testMarkNewEntitiesAsUncommitted() {
        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        SchemaManager schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);
        setUpSchema(schemaManager);

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg");
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction t = manager.executeMutation(request);

        Map<Object, Object> valueMap = traversalSource.V().hasLabel("Photo").valueMap().next();
        List<String> status = (List<String>) valueMap.get("status");
        assertEquals(1, status.size());
        assertEquals("UNCOMMITTED", status.get(0));
    }

    @Test
    @DisplayName("should be able to abort a mutation transaction")
    void testRollback() {
        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        SchemaManager schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);
        setUpSchema(schemaManager);

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg");
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction t = manager.executeMutation(request);
        manager.abort(t);

        assertFalse(traversalSource.V().has("transactionId", t.getId()).hasNext(), "found results with transaction id " + t.getId());
    }

    @Test
    @DisplayName("should store all allowed data types")
    public void testStoreAllDataTypes() {
        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        SchemaManager schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);
        setUpSchema(schemaManager);

        Date now = new Date();

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg")
                .intProperty("number", 5)
                .booleanProperty("boolean", true)
                .dateProperty("someDate", now)
                .binaryReferenceProperty("binary", 1L)
                .doubleProperty("double", 2.0)
                .intListProperty("intList", List.of(1, 2, 3))
                .booleanListProperty("booleanList", List.of(true, false, true))
                .doubleListProperty("doubleList", List.of(1.0, 2.0, 3.0))
                .dateListProperty("dateList", List.of(now))
                .stringListProperty("stringList", List.of("s1"));
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction t = manager.executeMutation(request);

        manager.prepare(t);
        manager.commit(t);

        Map<Object, Object> valueMap = traversalSource.V().hasLabel("Photo").valueMap().next();
        assertEquals(List.of("NORMAL"), valueMap.get("status"));
        assertEquals(List.of(t.getId()), valueMap.get("transactionId"));
        assertEquals(List.of("photo1.jpg"), valueMap.get("Photo_name"));
        assertEquals(List.of(5), valueMap.get("Photo_number"));
        assertEquals(List.of(true), valueMap.get("Photo_boolean"));
        assertEquals(List.of(now), valueMap.get("Photo_someDate"));
        assertEquals(List.of(1L), valueMap.get("Photo_binary"));
        assertEquals(List.of(2.0), valueMap.get("Photo_double"));
        assertEquals(List.of( 1, 2, 3), valueMap.get("Photo_intList"));
        assertEquals(List.of(true, false, true), valueMap.get("Photo_booleanList"));
        assertEquals(List.of(1.0, 2.0, 3.0),valueMap.get("Photo_doubleList"));
        assertEquals(List.of(now), valueMap.get("Photo_dateList"));
        assertEquals(List.of("s1"), valueMap.get("Photo_stringList"));
    }

    private Traversal<Vertex, Map<String, Object>> propertiesAndRelationshipTraversal(GraphTraversal<Vertex, Vertex> start) {
        return start.project("id", "label", "props", "out", "in")
                .by(__.id())
                .by(__.label())
                .by(__.valueMap())
                .by(__.choose(
                                __.outE().count().is(0),

                                __.constant(new HashMap<>()),

                                __.outE()
                                        .unfold()
                                        .project("label", "info")
                                        .by(__.label())
                                        .by(__.project("properties", "target")
                                                .by(__.valueMap())
                                                .by(__.inV().project("id", "label", "props").by(__.id()).by(__.label()).by(__.valueMap()))
                                        )
                                        .group().by("label").by("info")
                        )
                )
                .by(__.choose(
                                __.inE().count().is(0),

                                __.constant(new HashMap<>()),

                                __.inE()
                                        .unfold()
                                        .project("label", "info")
                                        .by(__.label())
                                        .by(__.project("properties", "source")
                                                .by(__.valueMap())
                                                .by(__.inV().project("id", "label", "props").by(__.id()).by(__.label()).by(__.valueMap()))
                                        )
                                        .group().by("label").by("info")


                        )
                );
    }

    @Test
    @DisplayName("should be able to update an entity")
    public void testUpdateEntity() {
        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        SchemaManager schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);
        setUpSchema(schemaManager);

        LOG.info("Creating original entity");
        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg")
                .intProperty("number", 5);
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction t = manager.executeMutation(request);
        manager.prepare(t);
        manager.commit(t);


        LOG.info("Updating entity");
        Vertex v = traversalSource.V().hasLabel("Photo").next();
        entityMutation.id(v.id()).stringProperty("name", "photo2.jpg").intProperty("number", null).booleanProperty("boolean", false);
        t = manager.executeMutation(request);
        manager.prepare(t);

        var checkNodes = Streams.stream(traversalSource.V().valueMap()).toList();

        LOG.info("Updated entity");

        Traversal<Vertex, Map<String, Object>> uncommittedTraversal = propertiesAndRelationshipTraversal(traversalSource.V().hasLabel("Photo").has(PropertyConstants.TRANSACTION_ID_PROPERTY, t.getId()));
        assertTrue(uncommittedTraversal.hasNext());
        var uncommittedEntity = uncommittedTraversal.next();

        HashMap<String, Object> props = (HashMap<String, Object>)uncommittedEntity.get("props");
        ArrayList<String> status = (ArrayList)props.get("status");
        assertEquals(List.of("UNCOMMITTED"), status);

        manager.commit(t);

        var allNodes = traversalSource.V().hasLabel("Photo").project("id", "label", "props").by(__.id()).by(__.label()).by(__.valueMap());
        while (allNodes.hasNext()) {
            var next = allNodes.next();
            LOG.info("Next: {}", next);
        }

        var version1Traversal = propertiesAndRelationshipTraversal(traversalSource.V().hasLabel("Photo").has("Photo_name", "photo1.jpg"));
        assertTrue(version1Traversal.hasNext(), "version 1 not found");
        var v1 = version1Traversal.next();

        var version2Traversal = propertiesAndRelationshipTraversal(traversalSource.V().hasLabel("Photo").has("Photo_name", "photo2.jpg"));
        assertTrue(version2Traversal.hasNext(), "version 2 not found");
        var v2 = version2Traversal.next();

        HashMap<String, Object> v1Incoming = (HashMap<String, Object>)v1.get("in");
        List<Map<String, Object>> v1HasPrevious = (List<Map<String, Object>>)v1Incoming.get(LabelConstants.HAS_PREVIOUS_VERSION_LABEL);
        Map<String, Object> firstInfo = v1HasPrevious.get(0);
        Map<String, Object> source = (Map<String, Object>) firstInfo.get("source");
        assertEquals(v1.get("id"), source.get("id"));

        var v2Props = (Map<String, Object>)v2.get("props");
        assertEquals(List.of(false), v2Props.get("Photo_boolean"));
        assertEquals(List.of("photo2.jpg"), v2Props.get("Photo_name"));
        assertNull(v2Props.get("Photo_number"));
    }

}
