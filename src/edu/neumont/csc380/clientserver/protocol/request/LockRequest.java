package edu.neumont.csc380.clientserver.protocol.request;

public class LockRequest extends Request {
    public LockRequest(String key) {
        super(Type.LOCK, key);
    }
}
