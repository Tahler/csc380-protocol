package edu.neumont.csc380.scalablesystem.protocol.request;

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
        CONTAINS_KEY,
        PUT,
        GET,
        UPDATE,
        DELETE
    }

    @Override
    public String toString() {
        return "Request{" +
                "type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
