package edu.neumont.csc380.clientserver.models.repo;

public class KeyDoesNotExistException extends RuntimeException {
    public KeyDoesNotExistException() {
    }

    public KeyDoesNotExistException(String message) {
        super(message);
    }
}
