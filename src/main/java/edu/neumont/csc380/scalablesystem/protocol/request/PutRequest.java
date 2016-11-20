package edu.neumont.csc380.scalablesystem.protocol.request;

public class PutRequest extends Request {
    private Object value;

    public PutRequest(String key, Object value) {
        super(Type.PUT, key);

        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
