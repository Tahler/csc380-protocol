package edu.neumont.csc380.scalablesystem.models;

public interface Repository<T> {

    void put(String key, T value);

    T get(String key);

    void update(String key, T value);

    void delete(String key);
}
