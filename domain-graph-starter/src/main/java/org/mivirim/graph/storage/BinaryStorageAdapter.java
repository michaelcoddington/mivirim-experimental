package org.mivirim.graph.storage;

/**
 * Stores binary data and returns the hashes of that data.
 */
public interface BinaryStorageAdapter {

    BinaryDataWriter openWriter();

    BinaryHash close(BinaryDataWriter writer);

}
