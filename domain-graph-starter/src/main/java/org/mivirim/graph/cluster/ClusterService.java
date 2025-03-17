package org.mivirim.graph.cluster;

import com.hazelcast.cluster.Member;
import com.hazelcast.map.IMap;

import java.util.Set;

public interface ClusterService {

    /**
     * Returns true if this node is the leader node in the cluster.
     */
    boolean isLeaderNode();

    boolean isJoined();

    Set<Member> getClusterMembers();

    void addClusterJoinReaction(ClusterJoinReaction reaction);

    <K, V> IMap<K, V> getMap(String name);

}
