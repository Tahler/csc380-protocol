package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.*;
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

    public RingInfo copy() {
        return new RingInfo(this.timestamp, this.mappings);
    }

    public RingNodeInfo getNodeContainingKey(String key) {
        int hash = key.hashCode();
        return this.mappings.get(hash);
    }

    public void update(RingInfo other) {
        if (other.timestamp > this.timestamp) {
            this.mappings = other.mappings;
        }
    }

    public static void main(String[] args) {
//        Multiset<KeyRange> ms = TreeMultiset.from(Arrays.asList(
//                new KeyRange(8, new RingNode("localhost", 8081)),
//                new KeyRange(0, new RingNode("localhost", 8080))
//        ));

        RangeMap<Integer, RingNodeInfo> map = TreeRangeMap.create();
        map.put(HashRange.from(0, 8), new RingNodeInfo("localhost", 8080));
        map.put(HashRange.from(8, 16), new RingNodeInfo("localhost", 8081));
        System.out.println(map.get(3));
        System.out.println(map.get(0));
        System.out.println(map.get(8));
        System.out.println(map.get(16));

        System.out.println(map.span());
    }

    private static class HashRange {
        public static Range<Integer> from(int lower, int upper) {
            return Range.closedOpen(lower, upper);
        }
    }
}
