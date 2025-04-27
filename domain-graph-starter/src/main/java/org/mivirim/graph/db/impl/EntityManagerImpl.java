package org.mivirim.graph.db.impl;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.mivirim.graph.db.EntityManager;
import org.mivirim.graph.db.PropertyNameTranslator;
import org.mivirim.graph.db.Transaction;
import org.mivirim.graph.db.mutation.EntityMutation;
import org.mivirim.graph.db.mutation.ListProperty;
import org.mivirim.graph.db.mutation.MutationRequest;
import org.mivirim.graph.db.mutation.Property;
import org.mivirim.graph.db.mutation.ScalarProperty;
import org.mivirim.graph.db.query.Query;
import org.mivirim.graph.db.query.Result;
import org.mivirim.graph.db.storage.BinaryHash;
import org.mivirim.graph.db.storage.BinaryStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mivirim.graph.LabelConstants.DATA_LABEL;

@Service
public class EntityManagerImpl implements EntityManager {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerImpl.class);

    private GraphTraversalSource traversalSource;
    private BinaryStorageAdapter binaryStorageAdapter;

    public EntityManagerImpl(GraphTraversalSource traversalSource, BinaryStorageAdapter binaryStorageAdapter) {
        this.traversalSource = traversalSource;
        this.binaryStorageAdapter = binaryStorageAdapter;
    }

    @Override
    public void resetGraph() {
        LOG.warn("Resetting graph. Dropping all vertices and edges.");
        var transaction = traversalSource.tx();
        transaction.begin();
        traversalSource.V().drop();
        traversalSource.E().drop();
        transaction.commit();
        transaction.close();
    }

    /* -------------------------------- Binary methods -------------------------------- */

    @Override
    public HashingBinaryDataWriter openWriter() throws IOException {
        return binaryStorageAdapter.openWriter();
    }

    @Override
    public String commitBinary(HashingBinaryDataWriter writer) throws IOException {
        BinaryHash hash = binaryStorageAdapter.close(writer);
        var iterator = traversalSource.V().hasLabel(DATA_LABEL)
                .has("sha256", hash.getSha256Hash())
                .has("md5", hash.getMd5Hash())
                .elementMap();
        if (iterator.hasNext()) {
            return (String) iterator.next().get("uid");
        } else {
            var transaction = traversalSource.tx();
            String uid;
            GraphTraversal<Vertex, Vertex> traversal;
            do {
                uid = UUID.randomUUID().toString();
                traversal = traversalSource.V().hasLabel(DATA_LABEL).has("uid", uid);
            } while (traversal.hasNext());

            traversalSource.addV(DATA_LABEL).property(Map.of(
                    "sha256", hash.getSha256Hash(),
                    "md5", hash.getMd5Hash(),
                    "uid", uid
            )).next();

            transaction.commit();
            return uid;
        }
    }

    @Override
    public void abandonBinary(HashingBinaryDataWriter writer) {
        binaryStorageAdapter.abandon(writer);
    }

    /* -------------------------------- Transaction methods -------------------------------- */

    @Override
    public Transaction executeMutation(MutationRequest mutationRequest) {
        LOG.info("Executing mutation {}", mutationRequest);

        Transaction t = new Transaction(UUID.randomUUID().toString());

        var transaction = traversalSource.tx();

        // TODO: this will need to be unique within the graph!
        Set<String> uids = new HashSet<>();

        GraphTraversal<Vertex, Vertex> traversal = null;
        for (EntityMutation entityMutation : mutationRequest.getEntityMutations()) {
            String sourceId;
            do {
                sourceId = UUID.randomUUID().toString();
            } while (uids.contains(sourceId));

            uids.add(sourceId);

            traversal = traversalSource.addV(entityMutation.getEntityType()).as(sourceId).property("uid", sourceId);

            for (Property property : entityMutation.getProperties()) {
                String translatedPropertyName = PropertyNameTranslator.externalPropertyNameToInternalName(entityMutation.getEntityType(), property.getName());
                if (property instanceof ScalarProperty<?, ?> scalarProperty) {
                    traversal = traversal.property(translatedPropertyName, scalarProperty.getValue());
                } else if (property instanceof ListProperty<?> listProperty) {
                    for (Object value : listProperty.getValues()) {
                        traversal = traversal.property(VertexProperty.Cardinality.list, translatedPropertyName, value);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported property type: " + property.getClass());
                }
            }
        }

        while (traversal != null && traversal.hasNext()) {
            traversal.next();
        }

        transaction.commit();

        return t;
    }

    @Override
    public void commit(Transaction transaction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void abort(Transaction transaction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Result executeQuery(Query query) {
        throw new RuntimeException("Not implemented yet");
    }
}
