package edu.neumont.csc380.clientserver.models.repo;

public class KeyIsLockedException extends RuntimeException {
    public KeyIsLockedException() { }

    public KeyIsLockedException(String message) {
        super(message);
    }
}
