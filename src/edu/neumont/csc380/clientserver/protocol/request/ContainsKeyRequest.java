package edu.neumont.csc380.clientserver.protocol.request;

public class ContainsKeyRequest extends Request {
    public ContainsKeyRequest(String key) {
        super(Type.CONTAINS_KEY, key);
    }
}
