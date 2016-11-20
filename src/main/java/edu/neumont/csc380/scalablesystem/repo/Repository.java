package edu.neumont.csc380.scalablesystem.repo;

public interface Repository<K, V> {

    boolean containsKey(K key);

    void put(K key, V value);

    V get(K key);

    void update(K key, V value);

    void delete(K key);
}
