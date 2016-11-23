package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import edu.neumont.csc380.scalablesystem.protocol.Protocol;

/**
 * Stores info about the cluster. Responsible for directing requests to the correct server.
 */
public class RingInfo {
    private long timestamp;
    private RangeMap<Integer, RingNodeInfo> mappings;

    public RingInfo() {
        this(
                System.currentTimeMillis(),
                ImmutableRangeMap.of(Range.all(), new RingNodeInfo(Protocol.HOST, Protocol.START_PORT)));
    }

    public RingInfo(long timestamp, RangeMap<Integer, RingNodeInfo> mappings) {
        this.timestamp = timestamp;
        this.mappings = mappings;
    }

    public RingNodeInfo getNodeWithKey(String key) {
        int hash = key.hashCode();
        return this.mappings.get(hash);
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
