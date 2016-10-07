package edu.neumont.csc380.clientserver.protocol.request;

public class DeleteRequest extends Request {
    public DeleteRequest(String key) {
        super(Type.DELETE, key);
    }
}
