package org.mivirim.graph.schema.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mivirim.graph.cluster.ClusterService;
import org.mivirim.graph.schema.SchemaChangeCoordinator;
import org.mivirim.graph.schema.SchemaChangeReaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchemaChangeCoordinatorImpl implements SchemaChangeCoordinator, EntryAddedListener<String, Object>, EntryUpdatedListener<String, Object> {

    private static final Logger LOG = LogManager.getLogger(SchemaChangeCoordinatorImpl.class);

    private static final String SCHEMA_MAP_NAME = "schemaMap";
    private static final String SCHEMA_VERSION_LABEL = "version";

    private IMap<String, Object> schemaMap;
    private List<SchemaChangeReaction> reactions = new ArrayList<>();

    public SchemaChangeCoordinatorImpl(ClusterService clusterService) {
        this.schemaMap = clusterService.getMap(SCHEMA_MAP_NAME);
        this.schemaMap.addEntryListener(this, true);
    }

    @Override
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
