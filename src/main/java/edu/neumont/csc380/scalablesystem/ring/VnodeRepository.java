package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.Config;
import edu.neumont.csc380.scalablesystem.comparator.HashComparator;
import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.ring.repo.KeyDoesNotExistException;
import edu.neumont.csc380.scalablesystem.ring.repo.RepositoryFullException;
import edu.neumont.csc380.scalablesystem.ring.repo.RxHallaStor;
import rx.*;
import rx.Observable;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

// I AM ASSUMING THAT THE REQUEST ALWAYS GOES TO THE FIRST NODE IN THE VNODE AT FIRST
public class VnodeRepository implements RxHallaStor {
    protected TreeSet<String> keySet;
    private final RingNodeInfo firstSubNode;

    public VnodeRepository(RingNodeInfo localNodeInfo) {
        this.firstSubNode = calculateFirstSubNode(localNodeInfo);
        this.keySet = new TreeSet<>(new HashComparator<>());
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
        Env.LOGGER.debug("VNODE : poll last");
        Env.LOGGER.debug(this.keySet.stream().map(String::hashCode).collect(Collectors.toList()));
        String key = this.keySet.last();
        Env.LOGGER.debug("VNODE : last key = " + key + " (" + key.hashCode() +")");
        return this.get(key)
                .map(value -> {
                    Env.LOGGER.debug("VNODE : deleting " + key + "...");
                    this.delete(key).await();
                    Env.LOGGER.debug("VNODE : deleted " + key);
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
            if (currentSize >= Config.VNODE_CAPACITY) {
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
                List<RemoteIntercomRepository> intercomRepos = this.getIntercomReposToSubNodes();
                final boolean[] got = {false};
                for (RemoteIntercomRepository repo : intercomRepos) {
                    if (!got[0]) {
                        Env.LOGGER.debug("VNODE : trying to get from next in intercoms");
                        repo.get(key).subscribe(
                                value -> {
                                    got[0] = true;
                                    Env.LOGGER.debug("VNODE : got");
                                    subscriber.onSuccess(value);
                                },
                                err -> Env.LOGGER.debug("VNODE : did not get"));
                    }
                }
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
        return Completable.create(subscriber -> {
            if (this.keySet.contains(key)) {
                // Linear search through intercom, return first with it
                // TODO: can probably be parallel
                List<RemoteIntercomRepository> intercomRepos = this.getIntercomReposToSubNodes();
                for (RemoteIntercomRepository repo : intercomRepos) {
                    repo.delete(key).await();
                }
                subscriber.onCompleted();
            } else {
                subscriber.onError(new KeyDoesNotExistException());
            }
        });
    }

    public static RingNodeInfo calculateFirstSubNode(RingNodeInfo localNodeInfo) {
        int startingPort = Config.START_PORT;
        int myPort = localNodeInfo.port;
        int vnodeSize = Config.VNODE_SIZE;
        int numNodesBelow = (myPort - startingPort) / 2; // 2 comm ports per node
        int numVnodesBelow = numNodesBelow / vnodeSize;
        int vnodePortSize = vnodeSize * 2;
        int firstSubNodePort = startingPort + (vnodePortSize * numVnodesBelow);
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
