package edu.neumont.csc380.clientserver.protocol.response;

public class LockSuccessResponse extends Response {
    private Object value;

    public LockSuccessResponse(Object value) {
        super(Type.LOCK_SUCCESS);
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
