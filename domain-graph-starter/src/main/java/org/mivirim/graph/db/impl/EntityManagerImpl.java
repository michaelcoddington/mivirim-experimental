package org.mivirim.graph.db.impl;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.mivirim.graph.db.EntityManager;
import org.mivirim.graph.db.storage.BinaryHash;
import org.mivirim.graph.db.storage.BinaryStorageAdapter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.mivirim.graph.LabelConstants.DATA_LABEL;

@Service
public class EntityManagerImpl implements EntityManager {

    private GraphTraversalSource traversalSource;
    private BinaryStorageAdapter binaryStorageAdapter;

    public EntityManagerImpl(GraphTraversalSource traversalSource, BinaryStorageAdapter binaryStorageAdapter) {
        this.traversalSource = traversalSource;
        this.binaryStorageAdapter = binaryStorageAdapter;
    }

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

}
