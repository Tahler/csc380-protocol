package edu.neumont.csc380.scalablesystem.repo;

import com.hallaLib.HallaStor;
import edu.neumont.csc380.scalablesystem.comparator.HashComparator;
import edu.neumont.csc380.scalablesystem.ring.Node;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeSet;

public class LocalRepository implements RxHallaStor {
    // Sorted set of keys to aid with splitting
    private TreeSet<String> keys;
    private final HallaStor hallaStor;

    public LocalRepository() {
        this.keys = new TreeSet<>(new HashComparator<>());
        this.hallaStor = HallaStor.getInstance();
    }

    public Map.Entry<String, Object> peekLast() {
        String key = this.keys.last();
        Object value = this.hallaStor.get(key);
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * Retrieves and removes the key-value pair with the last key in the sorted key-set.
     * @return The key-value pair with the last key in the sorted set.
     */
    public Map.Entry<String, Object> pollLast() {
        String key = this.keys.pollLast();
        Object value = this.hallaStor.get(key);
        this.hallaStor.delete(key);
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public void push(Map.Entry<String, Object> entry) {
        this.keys.add(entry.getKey());
        this.hallaStor.add(entry.getKey(), entry.getValue());
    }

    public int size() {
        return this.keys.size();
    }

    public Single<Boolean> containsKey(String key) {
        return Single.just(this.keys.contains(key));
    }

    @Override
    public Completable put(String key, Object value) {
        Node.LOGGER.info("Putting " + key + " : " + value + "...");
        return Observable
                .create(subscriber -> {
                    if (this.keys.contains(key)) {
                        subscriber.onError(new KeyAlreadyExistsException());
                    } else {
                        try {
                            this.hallaStor.add(key, value);
                            this.keys.add(key);
                            Node.LOGGER.info("...done putting " + key + " : " + value + ".");
                            subscriber.onCompleted();
                        } catch (IllegalStateException e) {
                            subscriber.onError(new RepositoryFullException());
                        }
                    }
                })
                .toCompletable();
    }

    @Override
    public Single<Object> get(String key) {
        return Single
                .create(subscriber -> {
                    Object value = this.hallaStor.get(key);
                    if (value == null) {
                        subscriber.onError(new KeyDoesNotExistException());
                    } else {
                        subscriber.onSuccess(value);
                    }
                });
    }

    @Override
    public Completable update(String key, Object value) {
        return Observable
                .create(subscriber -> {
                    if (this.keys.contains(key)) {
                        this.hallaStor.update(key, value);
                    } else {
                        subscriber.onError(new KeyDoesNotExistException());
                    }
                })
                .toCompletable();
    }

    @Override
    public Completable delete(String key) {
        return Observable
                .create(subscriber -> {
                    if (this.keys.contains(key)) {
                        this.hallaStor.delete(key);
                        this.keys.remove(key);
                    } else {
                        subscriber.onError(new KeyDoesNotExistException());
                    }
                })
                .toCompletable();
    }
}
