package edu.neumont.csc380.scalablesystem.repo;

import com.hallaLib.HallaStor;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.TreeSet;

public class LocalRepository implements RxHallaStor {
    // Sorted set of keys to aid with splitting
    private TreeSet<String> keys;
    private final HallaStor hallaStor;

    public LocalRepository() {
        this.keys = new TreeSet<>();
        this.hallaStor = HallaStor.getInstance();
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        return Single.just(this.keys.contains(key));
    }

    @Override
    public Completable put(String key, Object value) {
        return Observable
                .create(subscriber -> {
                    if (this.keys.contains(key)) {
                        subscriber.onError(new KeyAlreadyExistsException());
                    } else {
                        try {
                            this.hallaStor.add(key, value);
                            this.keys.add(key);
                            subscriber.onCompleted();
                        } catch (Exception e) {
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
