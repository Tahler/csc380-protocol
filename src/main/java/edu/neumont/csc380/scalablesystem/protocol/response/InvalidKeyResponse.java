package edu.neumont.csc380.scalablesystem.protocol.response;

public class InvalidKeyResponse extends Response {
    public InvalidKeyResponse() {
        super(Type.INVALID_KEY);
    }
}
