package edu.neumont.csc380.clientserver.protocol.response;

public class GetSuccessResponse extends Response {
    private Object value;

    public GetSuccessResponse(Object value) {
        super(Type.GET_SUCCESS);
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
