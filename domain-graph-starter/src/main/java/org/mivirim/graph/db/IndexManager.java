package org.mivirim.graph.db;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface IndexManager {

    void dropIndex() throws InterruptedException, ExecutionException;

    Set<IndexField> getIndexFields();

    void addIndexChangeReaction(IndexChangeReaction reaction);

}
