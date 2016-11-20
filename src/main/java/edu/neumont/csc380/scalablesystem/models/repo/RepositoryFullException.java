package edu.neumont.csc380.scalablesystem.models.repo;

public class RepositoryFullException extends RuntimeException {
    public RepositoryFullException() { }

    public RepositoryFullException(String message) {
        super(message);
    }
}
