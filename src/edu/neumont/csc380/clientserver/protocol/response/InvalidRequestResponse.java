package edu.neumont.csc380.clientserver.protocol.response;

public class InvalidRequestResponse extends Response {
    public InvalidRequestResponse() {
        super(Type.INVALID_REQUEST);
    }
}
