package edu.neumont.csc380.clientserver.protocol.request;

public class UpdateRequest extends Request {
    private Object value;

    public UpdateRequest(String key, Object value) {
        super(Type.UPDATE, key);
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
