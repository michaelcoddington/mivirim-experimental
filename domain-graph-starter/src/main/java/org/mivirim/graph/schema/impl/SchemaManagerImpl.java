package org.mivirim.graph.schema.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.mivirim.graph.DuplicateException;
import org.mivirim.graph.LabelConstants;
import org.mivirim.graph.schema.EntityDefinition;
import org.mivirim.graph.schema.PropertyDefinition;
import org.mivirim.graph.schema.PropertyGroupDefinition;
import org.mivirim.graph.schema.PropertyType;
import org.mivirim.graph.schema.RelationshipDefinition;
import org.mivirim.graph.schema.SchemaChangeCoordinator;
import org.mivirim.graph.schema.SchemaManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mivirim.graph.LabelConstants.SCHEMA_LABEL;

@Service
public class SchemaManagerImpl implements SchemaManager {

    private static final Logger LOG = LogManager.getLogger(SchemaManagerImpl.class);

    private final GraphTraversalSource traversalSource;

    private final SchemaChangeCoordinator schemaChangeCoordinator;

    public SchemaManagerImpl(GraphTraversalSource traversalSource, SchemaChangeCoordinator schemaChangeCoordinator) {
        this.traversalSource = traversalSource;
        this.schemaChangeCoordinator = schemaChangeCoordinator;
    }

    @Override
    public Set<EntityDefinition> retrieveEntitySchemas() {
        GraphTraversal<Vertex, Vertex> start = traversalSource.V().hasLabel(SCHEMA_LABEL);
        return retrieveEntitySchemas(start);
    }

    @Override
    public void createEntitySchema(EntityDefinition schema) {
        var tx = traversalSource.tx();
        try {
            traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", schema.getName()).next();
            throw new DuplicateException(String.format("Schema %s already exists", schema.getName()));
        } catch (NoSuchElementException er) {
            GraphTraversal<Vertex, ?> traversal = traversalSource.addV(SCHEMA_LABEL)
                    .property("name", schema.getName())
                    .property("description", schema.getDescription())
                    .as("schema");

            AtomicInteger vertexCount = new AtomicInteger(0);

            // add direct properties
            for (PropertyDefinition p: schema.getProperties()) {
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
            for (PropertyGroupDefinition groupDefinition: schema.getPropertyGroups()) {
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
            LOG.info("Created schema {}", schema);
            schemaChangeCoordinator.signalEntitySchemaChange();
        } finally {
            tx.commit();
            tx.close();
        }
    }

    @Override
    public void updateEntitySchema(EntityDefinition schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<EntityDefinition> retrieveEntitySchema(String name) {
        GraphTraversal<Vertex, Vertex> start = traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", name);
        Set<EntityDefinition> defs = retrieveEntitySchemas(start);
        if (defs.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(defs.iterator().next());
        }
    }

    private Set<EntityDefinition> retrieveEntitySchemas(GraphTraversal<Vertex, Vertex> startingTraversal) {
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
    public void deleteEntitySchema(EntityDefinition schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public Set<RelationshipDefinition> retrieveRelationshipSchemas() {
        return Set.of();
    }

    @Override
    public void createRelationshipSchema(RelationshipDefinition schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public void updateRelationshipSchema(RelationshipDefinition schema) {
        throw new RuntimeException("not done");
    }

    @Override
    public Optional<RelationshipDefinition> retrieveRelationshipSchema(String name) {
        throw new RuntimeException("not done");
    }

    @Override
    public void deleteRelationshipSchema(RelationshipDefinition schema) {
        throw new RuntimeException("not done");
    }
}
