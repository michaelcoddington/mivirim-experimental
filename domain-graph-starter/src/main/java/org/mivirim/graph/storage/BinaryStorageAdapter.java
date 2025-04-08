package org.mivirim.graph.storage;

import org.mivirim.graph.storage.impl.BinaryDataWriter;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * Stores binary data and returns the hashes of that data.
 */
public interface BinaryStorageAdapter {

    BinaryDataWriter openWriter() throws IOException, NoSuchAlgorithmException;

    BinaryHash close(BinaryDataWriter writer) throws IOException;

    InputStream openReader(BinaryHash hash) throws IOException;

    void abandon(BinaryDataWriter writer);

}
