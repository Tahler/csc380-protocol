package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.Config;
import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.ring.repo.KeyDoesNotExistException;
import edu.neumont.csc380.scalablesystem.ring.repo.RepositoryFullException;
import edu.neumont.csc380.scalablesystem.ring.repo.RxHallaStor;
import rx.*;

import java.util.*;
import java.util.stream.Collectors;

// I AM ASSUMING THAT THE REQUEST ALWAYS GOES TO THE FIRST NODE IN THE VNODE AT FIRST
public class VnodeRepository implements RxHallaStor {
    protected TreeSet<String> keySet;
    private final RingNodeInfo firstSubNode;

    public VnodeRepository(RingNodeInfo localNodeInfo) {
        this.firstSubNode = calculateFirstSubNode(localNodeInfo);
        this.keySet = new TreeSet<>();
    }

    public Single<Map.Entry<String, Object>> peekLast() {
        String key = this.keySet.last();
        return this.get(key).map(value -> new AbstractMap.SimpleEntry<>(key, value));
    }

    /**
     * Retrieves and removes the key-value pair with the last key in the sorted key-set.
     *
     * @return The key-value pair with the last key in the sorted set.
     */
    public Single<Map.Entry<String, Object>> pollLast() {
        String key = this.keySet.last();
        return this.get(key)
                .map(value -> {
                    this.delete(key).await();
                    return new AbstractMap.SimpleEntry<>(key, value);
                });
    }

    public int size() {
        return this.keySet.size();
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        return Single.just(this.keySet.contains(key));
    }

    @Override
    public Completable put(String key, Object value) {
        return Completable.create(subscriber -> {
            int currentSize = this.keySet.size();
            if (currentSize + 1 > Config.VNODE_CAPACITY) {
                Env.LOGGER.debug("VNODE : full. size = " + currentSize);
                subscriber.onError(new RepositoryFullException());
            } else {
                Env.LOGGER.debug("VNODE : putting " + key + " : " + value);
                // Linear search through intercom, add to keyset on each, try to add kvp to one other
                final int[] numToReplicate = {Config.REPLICATION_FACTOR};
                // TODO: could store what nodes are full to know not to try and add again
                for (RemoteIntercomRepository intercom : this.getIntercomReposToSubNodes()) {
                    if (numToReplicate[0] > 0) {
                        Env.LOGGER.debug("VNODE : asking " + intercom + " to put " + key + " : " + value);
                        intercom.put(key, value)
                                .subscribe(
                                        () -> numToReplicate[0] -= 1,
                                        err -> Env.LOGGER.debug("VNODE : he was full"));
                    } else {
                        intercom.put(key, null).await();
                    }
                }
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Single<Object> get(String key) {
        return Single.create(subscriber -> {
            if (this.keySet.contains(key)) {
                // Linear search through intercom, return first with it
                List<rx.Observable<Object>> gets = this.getIntercomReposToSubNodes().stream()
                        .map(intercom ->
                                intercom.get(key)
                                        .onErrorReturn(err -> null)
                                        .toObservable())
                        .collect(Collectors.toList());
                Single<Object> first = rx.Observable.merge(gets).first(Objects::nonNull).toSingle();
                first.subscribe(subscriber::onSuccess);
            } else {
                subscriber.onError(new KeyDoesNotExistException());
            }
        });
    }

    @Override
    public Completable update(String key, Object value) {
        // TODO:
        return null;
    }

    @Override
    public Completable delete(String key) {
        // Linearly through intercom, remove
        return this.keySet.contains(key)
                ? Completable.merge(
                this.getIntercomReposToSubNodes().stream()
                        .map(repo -> repo.delete(key))
                        .collect(Collectors.toList()))
                : Completable.error(new KeyDoesNotExistException());
    }

    public static RingNodeInfo calculateFirstSubNode(RingNodeInfo localNodeInfo) {
        int startingPort = Config.START_PORT;
        int myPort = localNodeInfo.port;
        int vnodeSize = Config.VNODE_SIZE;
        int numNodesBelow = (myPort - startingPort) / 2; // 2 comm ports per node
        int numVnodesBelow = numNodesBelow / vnodeSize;
        int firstSubNodePort = startingPort + (vnodeSize * numVnodesBelow);
        return new RingNodeInfo(Config.HOST, firstSubNodePort);
    }

    private List<RemoteIntercomRepository> getIntercomReposToSubNodes() {
        RingNodeInfo currentNode = this.firstSubNode;
        List<RemoteIntercomRepository> intercomRepos = new ArrayList<>(Config.VNODE_SIZE);
        for (int i = 0; i < Config.VNODE_SIZE; i++) {
            RemoteIntercomRepository remoteRepo = new RemoteIntercomRepository(currentNode);
            intercomRepos.add(remoteRepo);
            currentNode = RingInfo.getNextNodeInRing(currentNode);
        }
        return intercomRepos;
    }

    public RingNodeInfo getFirstSubNode() {
        return this.firstSubNode;
    }
}
