package edu.neumont.csc380.scalablesystem.protocol.request;

public class DeleteRequest extends Request {
    public DeleteRequest(String key) {
        super(Type.DELETE, key);
    }
}
