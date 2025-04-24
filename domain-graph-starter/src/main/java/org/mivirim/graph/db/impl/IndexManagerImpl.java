package org.mivirim.graph.db.impl;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.graphdb.database.management.GraphIndexStatusReport;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.mivirim.graph.db.IndexChangeReaction;
import org.mivirim.graph.db.IndexField;
import org.mivirim.graph.db.IndexManager;
import org.mivirim.graph.db.schema.EntityDefinition;
import org.mivirim.graph.db.schema.PropertyDefinition;
import org.mivirim.graph.db.schema.PropertyGroupDefinition;
import org.mivirim.graph.db.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the search indexes used by Janus to keep indexes in line with changes to
 * a schema.
 */
@Service
public class IndexManagerImpl implements IndexManager {

    private static final Logger LOG = LoggerFactory.getLogger(IndexManagerImpl.class);

    private static final String INDEX_NAME = "graphIndex";

    private static final String TYPE_PROPERTY_NAME = "_type";

    private final JanusGraph graph;
    private final SchemaManager schemaManager;

    private List<IndexChangeReaction> reactions = new ArrayList<>();

    public IndexManagerImpl(JanusGraph graph, SchemaManager schemaManager) {
        this.graph = graph;
        this.schemaManager = schemaManager;
        updateIndexes();
        schemaManager.addSchemaChangeReaction(this::updateIndexes);
    }

    private void updateIndexes() {
        LOG.info("Updating graph indexes");

        try {
            JanusGraphManagement management = graph.openManagement();

            // we use the "_type" property as an indexable substitute for vertex label
            JanusGraphIndex index = management.getGraphIndex(INDEX_NAME);
            if (index == null) {
                PropertyKey typeKey;
                if (management.containsPropertyKey(TYPE_PROPERTY_NAME)) {
                   typeKey = management.getPropertyKey(TYPE_PROPERTY_NAME);
                } else {
                    typeKey = management.makePropertyKey(TYPE_PROPERTY_NAME).dataType(String.class).make();
                }

                index = management.buildIndex(INDEX_NAME, Vertex.class)
                        .addKey(typeKey)
                        .buildMixedIndex("search");
                management.commit();

                GraphIndexStatusReport report = ManagementSystem.awaitGraphIndexStatus(graph, INDEX_NAME).status(SchemaStatus.ENABLED).call();
                LOG.info("Index status: {}", report);
            }

            // next, we need to check to see if there are properties defined on any entity definitions that are not in the index.
            // if there are any, we need to reindex so those fields can be used for later queries.


            // Get all entity definitions
            Set<EntityDefinition> definitions = schemaManager.retrieveEntityDefinitions();

            // For each entity, reduce the properties and property groups down to a list of properties
            Set<PropertyDefinition> propertyDefinitions = definitions.stream().map(EntityDefinition::getProperties).flatMap(Set::stream).collect(Collectors.toSet());
            Set<PropertyDefinition> groupedPropertyDefinitions = definitions.stream()
                    .map(EntityDefinition::getPropertyGroups).flatMap(Set::stream)
                    .map(PropertyGroupDefinition::getProperties).flatMap(Set::stream)
                    .collect(Collectors.toSet());

            // Reduce this to a list of unique property names
            Stream<PropertyDefinition> allProperties = Stream.concat(propertyDefinitions.stream(), groupedPropertyDefinitions.stream());

            // Compare that to the list of indexed properties


            // Add missing properties to the index
            // Start reindex
            for (IndexChangeReaction reaction : reactions) {
                reaction.indexChangesd();
            }

        } catch (InterruptedException ie) {
            LOG.error("Error while updating graph indexes", ie);
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public Set<IndexField> getIndexFields() {
        JanusGraphManagement management = graph.openManagement();
        JanusGraphIndex index = management.getGraphIndex(INDEX_NAME);
        PropertyKey[] keys = index.getFieldKeys();
        return Arrays.stream(keys).map(key -> new IndexField(key, index.getIndexStatus(key))).collect(Collectors.toSet());
    }

    @Override
    public void addIndexChangeReaction(IndexChangeReaction reaction) {
        reactions.add(reaction);
    }

    @Override
    public void dropIndex() throws InterruptedException, ExecutionException {
        JanusGraphManagement management = graph.openManagement();
        JanusGraphIndex index = management.getGraphIndex(INDEX_NAME);
        if (index != null) {
            var disable = management.updateIndex(index, SchemaAction.DISABLE_INDEX);
            management.commit();
            disable.get();

            ManagementSystem.awaitGraphIndexStatus(graph, INDEX_NAME).status(SchemaStatus.DISABLED).call();

            management = graph.openManagement();
            var discard = management.updateIndex(index, SchemaAction.DISCARD_INDEX);
            management.commit();
            discard.get();

            ManagementSystem.awaitGraphIndexStatus(graph, INDEX_NAME).status(SchemaStatus.DISCARDED).call();

            management = graph.openManagement();
            var drop = management.updateIndex(index, SchemaAction.DROP_INDEX);
            management.commit();
            drop.get();
        }

    }
}
