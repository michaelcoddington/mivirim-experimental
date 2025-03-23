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

    private GraphTraversalSource traversalSource;

    public SchemaManagerImpl(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    @Override
    public Set<EntityDefinition> retrieveEntitySchemas() {
        EntityDefinition coverSchema = new EntityDefinition();
        coverSchema.setName("Cover");
        return Set.of(coverSchema);
    }

    @Override
    public void createEntitySchema(EntityDefinition schema) {
        var tx = traversalSource.tx();
        try {
            traversalSource.V().hasLabel(SCHEMA_LABEL).has("name", schema.getName()).next();
            throw new DuplicateException(String.format("Schema %s already exists", schema.getName()));
        } catch (NoSuchElementException er) {
            GraphTraversal<Vertex, ?> v = traversalSource.addV(SCHEMA_LABEL)
                    .property("name", schema.getName()).as("schema");

            AtomicInteger vertexCount = new AtomicInteger(0);

            // add direct properties
            for (PropertyDefinition p: schema.getProperties()) {
                String ref = String.valueOf(vertexCount.getAndIncrement());

                v = v.addV(LabelConstants.SCHEMA_PROPERTY_LABEL)
                        .property("name", p.getName())
                        .property("type", p.getType().toString())
                        .as(ref);
                v = v.addE("has-property")
                        .from("schema").to(ref);
            }

            for (PropertyGroupDefinition groupDefinition: schema.getPropertyGroups()) {
                String groupRef = String.valueOf(vertexCount.getAndIncrement());

                v = v.addV(LabelConstants.SCHEMA_PROPERTY_GROUP_LABEL)
                        .property("name", groupDefinition.getName())
                        .as(groupRef);
                v = v.addE("has-property-group")
                        .from("schema").to(groupRef);

                for (PropertyDefinition groupPropertyDef: groupDefinition.getProperties()) {
                    String ref = String.valueOf(vertexCount.getAndIncrement());

                    v = v.addV(LabelConstants.SCHEMA_PROPERTY_LABEL)
                            .property("name", groupPropertyDef.getName())
                            .property("type", groupPropertyDef.getType().toString())
                            .as(ref);
                    v = v.addE("has-property")
                            .from(groupRef).to(ref);
                }
            }

            v.next();
            LOG.info("Created schema {}", schema);
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
        GraphTraversal<Vertex, Map<String, Object>> iter = traversalSource.V()
                .hasLabel(SCHEMA_LABEL)
                .has("name", name)
                .project("elements", "propertyNodes", "propertyGroupNodes")
                // elements of the schema node
                .by(__.elementMap())
                // a list of the property nodes tied to the schema node
                .by(__.out("has-property").elementMap().fold())
                // a map of the property groups tied to the schema and the properties in them
                .by(
                        __.out("has-property-group")
                                .project("propertyGroup", "propertyNodes")
                                .by(__.elementMap())
                                .by(__.out("has-property").elementMap().fold())
                                .fold()
                );
        if (iter.hasNext()) {
            Map<String, Object> v = iter.next();
            EntityDefinition schema = new EntityDefinition();
            var propertyMap = (HashMap) v.get("elements");
            schema.setName((String) propertyMap.get("name"));

            ArrayList<Map<String, Object>> properties = (ArrayList) v.get("propertyNodes");
            Set<PropertyDefinition> schemaProps = properties.stream()
                    .map(map -> {
                        PropertyDefinition p = new PropertyDefinition();
                        p.setName((String) map.get("name"));
                        p.setType(PropertyType.valueOf((String) map.get("type")));
                        return p;
                    }).collect(Collectors.toSet());
            schema.setProperties(schemaProps);

            ArrayList<Map<String, Object>> propertyGroups = (ArrayList) v.get("propertyGroupNodes");

            return Optional.of(schema);
        } else {
            return Optional.empty();
        }
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
