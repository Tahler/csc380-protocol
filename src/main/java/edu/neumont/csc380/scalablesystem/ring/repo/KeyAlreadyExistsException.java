package edu.neumont.csc380.scalablesystem.ring.repo;

public class KeyAlreadyExistsException extends RuntimeException {
    public KeyAlreadyExistsException() { }

    public KeyAlreadyExistsException(String message) {
        super(message);
    }
}
