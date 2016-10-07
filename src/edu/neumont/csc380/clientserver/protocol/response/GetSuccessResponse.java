package edu.neumont.csc380.clientserver.protocol.response;

import com.google.gson.JsonObject;
import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.Protocol;

public class GetSuccessResponse extends Response {
    private JsonObject value;

    public GetSuccessResponse(JsonObject value) {
        super(Type.GET_SUCCESS);

        this.value = value; //Protocol.deserializeTypedObject(value);
    }

    public JsonObject getValue() {
        return this.value;
    }
}
