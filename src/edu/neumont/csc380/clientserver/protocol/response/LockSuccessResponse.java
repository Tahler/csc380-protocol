package edu.neumont.csc380.clientserver.protocol.response;

import com.google.gson.JsonObject;

public class LockSuccessResponse extends Response {
    private JsonObject value;

    public LockSuccessResponse(JsonObject value) {
        super(Type.LOCK_SUCCESS);

        this.value = value; //Protocol.deserializeTypedObject(value);
    }

    public JsonObject getValue() {
        return this.value;
    }
}
