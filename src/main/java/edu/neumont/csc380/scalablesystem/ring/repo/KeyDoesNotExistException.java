package edu.neumont.csc380.scalablesystem.ring.repo;

public class KeyDoesNotExistException extends RuntimeException {
    public KeyDoesNotExistException() {
    }

    public KeyDoesNotExistException(String message) {
        super(message);
    }
}
