package edu.neumont.csc380.scalablesystem.ring.repo;

import rx.Completable;
import rx.Single;

public interface RxRepository<K, V> {

    Single<Boolean> containsKey(K key);

    Completable put(K key, V value);

    Single<V> get(K key);

    Completable update(K key, V value);

    Completable delete(K key);
}
