package edu.neumont.csc380.clientserver.models;

public interface Repository<T> {

    void put(String key, T value);

    T get(String key);

    void update(String key, T value);

    void delete(String key);
}
