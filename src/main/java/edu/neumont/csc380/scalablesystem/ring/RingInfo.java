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
        try {
            withAdded.put(range, node);
        } catch (Exception e) {
            Node.LOGGER.fatal(e);
        }
        //        ImmutableRangeMap.Builder<Integer, RingNodeInfo> builder = ImmutableRangeMap.builder();
//        Node.LOGGER.debug("builder - created: ");
//        builder.putAll(this.mappings);
//        Node.LOGGER.debug("builder - copied: ");
//        try {
//            builder.put(range, node);
//        } catch (Exception e){
//            Node.LOGGER.fatal(e);
//        }
//        Node.LOGGER.debug("builder - added: ");
//        this.mappings = builder.build();
//        Node.LOGGER.debug("builder - built: ");
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

    //    public void update(RingInfo other) {
//        if (other.timestamp > this.timestamp) {
//            this.mappings = other.mappings;
//        }
//    }

//    private static class HashRange {
//        public static Range<Integer> from(int lower, int upper) {
//            return Range.closedOpen(lower, upper);
//        }
//    }
}
