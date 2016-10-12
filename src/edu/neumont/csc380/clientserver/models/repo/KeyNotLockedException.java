package edu.neumont.csc380.clientserver.models.repo;

public class KeyNotLockedException extends RuntimeException {
    public KeyNotLockedException() { }

    public KeyNotLockedException(String message) {
        super(message);
    }
}
