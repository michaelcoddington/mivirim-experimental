package org.mivirim.graph.db;

import org.mivirim.graph.db.impl.HashingBinaryDataWriter;

import java.io.IOException;

public interface EntityManager {

    /**
     * Opens a writer that can be used to add binary data to the graph.
     */
    HashingBinaryDataWriter openWriter() throws IOException;

    /**
     * Commits binary data to the graph and returns its unique ID.
     */
    String commitBinary(HashingBinaryDataWriter writer) throws IOException;

    /**
     * Discards the binary data written by the given writer.
     * @param writer
     */
    void abandonBinary(HashingBinaryDataWriter writer);

}
