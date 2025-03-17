package org.mivirim.graph.schema.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvent;
import org.mivirim.graph.cluster.ClusterService;
import org.mivirim.graph.schema.SchemaChangeCoordinator;
import org.mivirim.graph.schema.SchemaChangeReaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchemaChangeCoordinatorImpl implements SchemaChangeCoordinator, EntryListener<String, Object> {

    private static final String SCHEMA_MAP_NAME = "schemaMap";
    private static final String SCHEMA_VERSION_LABEL = "version";

    private IMap<String, Object> schemaMap;
    private List<SchemaChangeReaction> reactions = new ArrayList<>();

    public SchemaChangeCoordinatorImpl(ClusterService clusterService) {
        this.schemaMap = clusterService.getMap(SCHEMA_MAP_NAME);
    }

    @Override
    public void signalEntitySchemaChange() {
        String uuid = UUID.randomUUID().toString();
        schemaMap.put(SCHEMA_VERSION_LABEL, uuid);
    }

    @Override
    public void addSchemaChangeReaction(SchemaChangeReaction reaction) {
        reactions.add(reaction);
    }

    private void entryChanged(EntryEvent<String, Object> entryEvent) {
        if (entryEvent.getKey().equals(SCHEMA_VERSION_LABEL)) {
            reactions.forEach(SchemaChangeReaction::onSchemaChange);
        }
    }

    /* Map entry listener methods */

    @Override
    public void entryAdded(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void entryEvicted(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void entryExpired(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void entryRemoved(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void entryUpdated(EntryEvent<String, Object> entryEvent) {
        entryChanged(entryEvent);
    }

    @Override
    public void mapCleared(MapEvent mapEvent) {
        // no-op
    }

    @Override
    public void mapEvicted(MapEvent mapEvent) {
        // no-op
    }
}
