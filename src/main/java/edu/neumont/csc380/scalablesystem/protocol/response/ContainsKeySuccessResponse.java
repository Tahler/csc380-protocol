package edu.neumont.csc380.scalablesystem.protocol.response;

public class ContainsKeySuccessResponse extends Response {
    private boolean containsKey;

    public ContainsKeySuccessResponse(boolean containsKey) {
        super(Type.CONTAINS_KEY_SUCCESS);
        this.containsKey = containsKey;
    }

    public boolean containsKey() {
        return this.containsKey;
    }
}
