package org.mivirim.graph.db;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mivirim.graph.JanusAutoConfiguration;
import org.mivirim.graph.cluster.config.StandaloneClusterConfigurationFactory;
import org.mivirim.graph.cluster.impl.ClusterServiceImpl;
import org.mivirim.graph.db.impl.IndexManagerImpl;
import org.mivirim.graph.db.schema.EntityDefinition;
import org.mivirim.graph.db.schema.PropertyDefinition;
import org.mivirim.graph.db.schema.PropertyType;
import org.mivirim.graph.db.schema.SchemaManager;
import org.mivirim.graph.db.schema.impl.SchemaManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = { JanusAutoConfiguration.class, SchemaManagerImpl.class, IndexManagerImpl.class, ClusterServiceImpl.class, StandaloneClusterConfigurationFactory.class })
public class IndexManagerTest {

    @Autowired
    private IndexManager indexManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private JanusGraph graph;

    @Test
    @DisplayName("initialize starting index")
    void initializeStartingIndex() {
        JanusGraphManagement management = graph.openManagement();
        PropertyKey key = management.getPropertyKey("_type");
        assertNotNull(key, "type key not created");
    }

    @Test
    @DisplayName("add keys to index when schema is added")
    void testAddKeysWhenSchemaIsAdded() {
        Set<EntityDefinition> entityDefinitions = schemaManager.retrieveEntityDefinitions();
        for (EntityDefinition entityDefinition : entityDefinitions) {
            schemaManager.deleteEntityDefinition(entityDefinition);
        }

        EntityDefinition definition = new EntityDefinition();
        definition.setName("Product");
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("name");
        propertyDefinition.setType(PropertyType.STRING);
        definition.setProperties(Set.of(propertyDefinition));

        AtomicReference<Boolean> ref = new AtomicReference<>(false);
        IndexChangeReaction reaction = () -> ref.set(true);
        indexManager.addIndexChangeReaction(reaction);

        schemaManager.createEntityDefinition(definition);
        await().atMost(5, TimeUnit.SECONDS).until(ref::get);

        Set<IndexField> indexFields = indexManager.getIndexFields();
        assertNotNull(indexFields);
        Optional<IndexField> nameFieldOpt = indexFields.stream().filter(field -> field.getPropertyKey().name().equals("name")).findFirst();
        assertTrue(nameFieldOpt.isPresent(), "index field for name not found");
        assertEquals("", nameFieldOpt.get().getStatus());
    }

}
