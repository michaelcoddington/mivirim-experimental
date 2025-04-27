package org.mivirim.graph.db;

import com.hazelcast.map.IMap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.JanusAutoConfiguration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = JanusAutoConfiguration.class)
@DisplayName("An entity manager")
public class EntityManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerTest.class);

    @Autowired
    private GraphTraversalSource traversalSource;

    @Autowired
    private JanusGraph janusGraph;

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

        EntityMutation entityMutation = new EntityMutation()
                .entityType("Photo")
                .stringProperty("name", "photo1.jpg");
        MutationRequest request = new MutationRequest(Set.of(entityMutation), Set.of());
        Transaction transaction = manager.executeMutation(request);
        assertNotNull(transaction, "null transaction");
    }

    @Test
    @DisplayName("should store all allowed data types")
    public void testStoreAllDataTypes() {
        BinaryStorageAdapter adapter = mock(BinaryStorageAdapter.class);
        EntityManager manager = new EntityManagerImpl(traversalSource, adapter);
        ClusterService clusterService = mock(ClusterService.class);
        doReturn(mock(IMap.class)).when(clusterService).getMap(anyString());
        SchemaManager schemaManager = new SchemaManagerImpl(janusGraph, traversalSource, clusterService);

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
        manager.executeMutation(request);

        Map<Object, Object> valueMap = traversalSource.V().hasLabel("Photo").valueMap().next();
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

}
