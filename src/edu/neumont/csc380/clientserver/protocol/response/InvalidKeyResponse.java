package edu.neumont.csc380.clientserver.protocol.response;

public class InvalidKeyResponse extends Response {
    public InvalidKeyResponse() {
        super(Type.INVALID_KEY);
    }
}
