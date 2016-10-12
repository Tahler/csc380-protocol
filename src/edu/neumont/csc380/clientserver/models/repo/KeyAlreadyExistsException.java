package edu.neumont.csc380.clientserver.models.repo;

public class KeyAlreadyExistsException extends RuntimeException {
    public KeyAlreadyExistsException() { }

    public KeyAlreadyExistsException(String message) {
        super(message);
    }
}
