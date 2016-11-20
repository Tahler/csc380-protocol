package edu.neumont.csc380.scalablesystem.models;

public class TypedObject<T> {
    private Type type;
    private T data;

    public TypedObject(Type type, T data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return this.type;
    }

    public T getData() {
        return this.data;
    }

    public enum Type {
        DRIVER,
        RACECAR
    }
}
