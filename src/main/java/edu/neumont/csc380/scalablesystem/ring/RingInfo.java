package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import edu.neumont.csc380.scalablesystem.Config;

import java.io.Serializable;

/**
 * Stores info about the cluster. Responsible for directing requests to the correct server.
 */
public class RingInfo implements Serializable {
    private long timestamp;
    // Hash range to first sub node of vnodes
    private RangeMap<Integer, RingNodeInfo> mappings;

    /**
     * Initializes the first RingInfo, spanning all ranges to the first node.
     */
    public RingInfo() {
        this(
                System.currentTimeMillis(),
                // TODO: this needs to coordinate with Spawner, will be hard with replication
                getFullRange());
    }

    public static RangeMap<Integer, RingNodeInfo> getFullRange() {
        return ImmutableRangeMap.of(Range.all(), new RingNodeInfo(Config.HOST, Config.START_PORT));
    }

    public RingInfo(long timestamp, RangeMap<Integer, RingNodeInfo> mappings) {
        this.timestamp = timestamp;
        this.mappings = mappings;
    }

    public RingNodeInfo getNodeWithKey(String key) {
        int hash = key.hashCode();
        return this.mappings.get(hash);
    }

    public void addNode(Range<Integer> range, RingNodeInfo node) {
        TreeRangeMap<Integer, RingNodeInfo> withAdded = TreeRangeMap.create();
        withAdded.putAll(this.mappings);
        withAdded.put(range, node);
        this.mappings = ImmutableRangeMap.copyOf(withAdded);
        this.timestamp();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public RangeMap<Integer, RingNodeInfo> getMappings() {
        return this.mappings;
    }

    private void timestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "RingInfo{" +
                "timestamp=" + timestamp +
                ", mappings=" + mappings +
                '}';
    }

    public static RingNodeInfo getNextNodeInRing(RingNodeInfo currentNodeInfo) {
        return new RingNodeInfo(currentNodeInfo.host, currentNodeInfo.port + 2);
    }
}
