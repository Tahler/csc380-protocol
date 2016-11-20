package edu.neumont.csc380.scalablesystem.protocol.request;

public class ContainsKeyRequest extends Request {
    public ContainsKeyRequest(String key) {
        super(Type.CONTAINS_KEY, key);
    }
}
