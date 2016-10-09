package edu.neumont.csc380.clientserver.protocol.request;

public abstract class Request {
    private Type type;
    private String key;

    public Request(Type type, String key) {
        this.type = type;
        this.key = key;
    }

    public Type getType() {
        return this.type;
    }

    public String getKey() {
        return this.key;
    }

    public enum Type {
        GET,
        PUT,
        LOCK,
        UPDATE,
        DELETE
    }
}
