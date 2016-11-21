package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.*;
import edu.neumont.csc380.scalablesystem.protocol.Protocol;
import edu.neumont.csc380.scalablesystem.repo.RemoteRepository;
import edu.neumont.csc380.scalablesystem.repo.RxHallaStor;
import rx.Completable;
import rx.Observable;

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

    public RxHallaStor getRepositoryWithKey(String key) {
        int hash = key.hashCode();
        RingNodeInfo nodeInfo = this.mappings.get(hash);
        return new RemoteRepository(nodeInfo);
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

//    public static void main(String[] args) {
////        Completable.complete()
//        Completable.error(new RuntimeException("uh oh"))
////        Completable.never()
//                .toObservable()
//                .onErrorReturn(e -> e)
//                .map(o -> o)
//                .toBlocking()
//                .subscribe(System.out::println);
////                .subscribe(o -> System.out.println("done"), err -> System.out.println(err));
//
//    }

    public static void main(String[] args) {
        Observable
                .create(subscriber -> {
                    throw new RuntimeException("thrown");
                })
                .map(couldBeErr -> "could be err")
                .onErrorReturn(err -> "not an err")
//                .toCompletable()
//                .onErrorComplete(err -> {
//                    System.out.println("hi");
//                    return false;
//                })
//                .toObservable()
//                .map(o -> {
//                    System.out.println("here");
//                    System.out.println(o);
//                    return o;
//                })
                .subscribe(o -> System.out.println("not err: " + o), err -> System.out.println("ps: " + err));

    }
}
