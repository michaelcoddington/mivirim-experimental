package org.mivirim.graph.db.schema.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.VertexLabel;
import org.mivirim.graph.DuplicateException;
import org.mivirim.graph.LabelConstants;
import org.mivirim.graph.cluster.ClusterService;
import org.mivirim.graph.db.schema.EntityDefinition;
import org.mivirim.graph.db.schema.PropertyDefinition;
import org.mivirim.graph.db.schema.PropertyGroupDefinition;
import org.mivirim.graph.db.schema.PropertyType;
import org.mivirim.graph.db.schema.RelationshipDefinition;
import org.mivirim.graph.db.schema.SchemaChangeReaction;
import org.mivirim.graph.db.schema.SchemaManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mivirim.graph.LabelConstants.SCHEMA_LABEL;

@Service
public class SchemaManagerImpl implements SchemaManager, EntryAddedListener<String, Object>, EntryUpdatedListener<String, Object> {

    private static final Logger LOG = LogManager.getLogger(SchemaManagerImpl.class);

    private static final String SCHEMA_MAP_NAME = "schemaMap";
    private static final String SCHEMA_VERSION_LABEL = "version";

    private IMap<String, Object> schemaMap;
    private List<SchemaChangeReaction> reactions = new ArrayList<>();

    private final JanusGraph janusGraph;

    private final GraphTraversalSource traversalSource;

    private final ClusterService clusterService;

    public SchemaManagerImpl(JanusGraph graph, GraphTraversalSource traversalSource, ClusterService clusterService) {
        this.janusGraph = graph;
        this.traversalSource = traversalSource;
        this.clusterService = clusterService;
        this.schemaMap = clusterService.getMap(SCHEMA_MAP_NAME);
        this.schemaMap.addEntryListener(this, true);
    }

    @Override
    public Set<EntityDefinition> retrieveEntityDefinitions() {
        GraphTraversal<Vertex, Vertex> start = traversalSource.V().hasLabel(SCHEMA_LABEL);
        return retrieveEntityDefinitions(start);
    }

    @Override
    public void createEntityDefinition(EntityDefinition definition) {
        var tx = traversalSource.tx();
        try {
            traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", definition.getName()).next();
            throw new DuplicateException(String.format("Schema %s already exists", definition.getName()));
        } catch (NoSuchElementException er) {
            VertexLabel typeLabel = janusGraph.makeVertexLabel(definition.getName()).make();
            LOG.info("Created new vertex label {}", typeLabel);

            GraphTraversal<Vertex, ?> traversal = traversalSource.addV(SCHEMA_LABEL)
                    .property("name", definition.getName())
                    .property("description", definition.getDescription())
                    .as("schema");

            AtomicInteger vertexCount = new AtomicInteger(0);

            // add direct properties
            for (PropertyDefinition p: definition.getProperties()) {
                String ref = String.valueOf(vertexCount.getAndIncrement());

                traversal = traversal.addV(LabelConstants.SCHEMA_PROPERTY_LABEL)
                        .property("name", p.getName())
                        .property("description", p.getDescription())
                        .property("type", p.getType().toString())
                        .as(ref);
                traversal = traversal.addE("has-property")
                        .from("schema").to(ref);
            }

            // add property groups
            Set<PropertyGroupDefinition> propertyGroups = definition.getPropertyGroups() == null ? Set.of() : definition.getPropertyGroups();

            for (PropertyGroupDefinition groupDefinition: propertyGroups) {
                String groupRef = String.valueOf(vertexCount.getAndIncrement());

                traversal = traversal.addV(LabelConstants.SCHEMA_PROPERTY_GROUP_LABEL)
                        .property("name", groupDefinition.getName())
                        .property("description", groupDefinition.getDescription())
                        .as(groupRef);
                traversal = traversal.addE("has-property-group")
                        .from("schema").to(groupRef);

                for (PropertyDefinition groupPropertyDef: groupDefinition.getProperties()) {
                    String ref = String.valueOf(vertexCount.getAndIncrement());

                    traversal = traversal.addV(LabelConstants.SCHEMA_PROPERTY_LABEL)
                            .property("name", groupPropertyDef.getName())
                            .property("description", groupPropertyDef.getDescription())
                            .property("type", groupPropertyDef.getType().toString())
                            .as(ref);
                    traversal = traversal.addE("has-property")
                            .from(groupRef).to(ref);
                }
            }

            traversal.next();
            tx.commit();
            LOG.info("Created definition {}", definition);
            signalEntitySchemaChange();
        } finally {
            tx.close();
        }
    }

    @Override
    public void updateEntityDefinition(EntityDefinition definition) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<EntityDefinition> retrieveEntityDefinition(String name) {
        GraphTraversal<Vertex, Vertex> start = traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", name);
        Set<EntityDefinition> defs = retrieveEntityDefinitions(start);
        if (defs.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(defs.iterator().next());
        }
    }

    private Set<EntityDefinition> retrieveEntityDefinitions(GraphTraversal<Vertex, Vertex> startingTraversal) {
        var elementProjectionName = "elements";
        var propertyNodeProjectionName = "propertyNodes";
        var propertyGroupProjectionName = "propertyGroups";

        GraphTraversal<Vertex, Map<String, Object>> iter = startingTraversal
                .project(elementProjectionName, propertyNodeProjectionName, propertyGroupProjectionName)
                // elements of the schema node
                .by(__.elementMap())
                // a list of the property nodes tied to the schema node
                .by(__.out("has-property").elementMap().fold())
                // a map of the property groups tied to the schema and the properties in them
                .by(
                        __.out("has-property-group")
                                .project(elementProjectionName, propertyNodeProjectionName)
                                .by(__.elementMap())
                                .by(__.out("has-property").elementMap().fold())
                                .fold()
                );

        return iter.toStream().map(v -> {
            EntityDefinition schema = new EntityDefinition();
            var propertyMap = (HashMap) v.get(elementProjectionName);
            schema.setName((String) propertyMap.get("name"));
            schema.setDescription((String) propertyMap.get("description"));

            ArrayList<Map<String, Object>> properties = (ArrayList) v.get(propertyNodeProjectionName);
            Set<PropertyDefinition> schemaProps = properties.stream()
                    .map(this::mapToPropertyDefinition).collect(Collectors.toSet());
            schema.setProperties(schemaProps);

            ArrayList<Map<String, Object>> propertyGroups = (ArrayList) v.get(propertyGroupProjectionName);
            Set<PropertyGroupDefinition> groups = propertyGroups.stream()
                    .map(this::mapToPropertyGroupDefinition)
                    .collect(Collectors.toSet());
            schema.setPropertyGroups(groups);
            return schema;
        }).collect(Collectors.toSet());
    }

    private PropertyDefinition mapToPropertyDefinition(Map<String, Object> map) {
        PropertyDefinition p = new PropertyDefinition();
        p.setName((String) map.get("name"));
        p.setDescription((String) map.get("description"));
        p.setType(PropertyType.valueOf((String) map.get("type")));
        return p;
    }

    private PropertyGroupDefinition mapToPropertyGroupDefinition(Map<String, Object> map) {
        PropertyGroupDefinition group = new PropertyGroupDefinition();
        Map<String, Object> elements = (Map) map.get("elements");
        ArrayList<Map<String, Object>> propertyNodes = (ArrayList) map.get("propertyNodes");
        group.setName((String) elements.get("name"));
        group.setDescription((String) elements.get("description"));
        Set<PropertyDefinition> groupProperties = propertyNodes.stream()
                .map(this::mapToPropertyDefinition).collect(Collectors.toSet());
        group.setProperties(groupProperties);
        return group;
    }

    @Override
    public void deleteEntityDefinition(EntityDefinition definition) {
        throw new RuntimeException("not done");
    }

    @Override
    public Set<RelationshipDefinition> retrieveRelationshipDefinitions() {
        return Set.of();
    }

    @Override
    public void createRelationshipDefinition(RelationshipDefinition definition) {
        throw new RuntimeException("not done");
    }

    @Override
    public void updateRelationshipDefinition(RelationshipDefinition definition) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<RelationshipDefinition> retrieveRelationshipDefinition(String name) {
        throw new RuntimeException("not done");
    }

    @Override
    public void deleteRelationshipDefinition(RelationshipDefinition definition) {
        throw new RuntimeException("not done");
    }

    public void signalEntitySchemaChange() {
        LOG.info("Signaling entity schema change");
        String uuid = UUID.randomUUID().toString();
        schemaMap.put(SCHEMA_VERSION_LABEL, uuid);
    }

    @Override
    public void addSchemaChangeReaction(SchemaChangeReaction reaction) {
        reactions.add(reaction);
    }

    private void entryChanged(EntryEvent<String, Object> entryEvent) {
        LOG.info("got entry event {}", entryEvent);
        if (entryEvent.getKey().equals(SCHEMA_VERSION_LABEL)) {
            reactions.forEach(SchemaChangeReaction::onSchemaChange);
        }
    }

    /* Map listener methods */

    @Override
    public void entryAdded(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void entryUpdated(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }
}
