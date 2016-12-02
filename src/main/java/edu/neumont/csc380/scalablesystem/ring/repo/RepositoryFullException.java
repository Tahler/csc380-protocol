package edu.neumont.csc380.scalablesystem.ring.repo;

public class RepositoryFullException extends RuntimeException {
    public RepositoryFullException() { }

    public RepositoryFullException(String message) {
        super(message);
    }
}
