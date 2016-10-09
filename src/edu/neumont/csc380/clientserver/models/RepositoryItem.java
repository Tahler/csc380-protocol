package edu.neumont.csc380.clientserver.models;

public class RepositoryItem<T> {
    private boolean locked;
    private T value;

    public RepositoryItem(T value) {
        this.locked = false;
        this.value = value;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T item) {
        this.value = item;
    }
}
