package edu.neumont.csc380.scalablesystem.protocol.request;

public class GetRequest extends Request {
    public GetRequest(String key) {
        super(Type.GET, key);
    }
}
