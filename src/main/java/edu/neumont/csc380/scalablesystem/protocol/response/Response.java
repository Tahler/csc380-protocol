package edu.neumont.csc380.scalablesystem.protocol.response;

public abstract class Response {
    private Type type;

    public Response(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        CONTAINS_KEY_SUCCESS,
        PUT_SUCCESS,
        GET_SUCCESS,
        LOCK_SUCCESS,
        UPDATE_SUCCESS,
        DELETE_SUCCESS,

        INVALID_REQUEST,
        INVALID_KEY,
        KEY_DOES_NOT_EXIST,
        KEY_ALREADY_EXISTS,
        KEY_LOCKED,
        KEY_NOT_LOCKED,
        SERVER_FULL
    }
}
