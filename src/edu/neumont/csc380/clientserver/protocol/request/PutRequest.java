package edu.neumont.csc380.clientserver.protocol.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.neumont.csc380.clientserver.models.TypedObject;

public class PutRequest extends Request {
    private JsonObject value;

    public PutRequest(String key, TypedObject value) {
        super(Type.PUT, key);

        Gson gson = new Gson();
        this.value = gson.fromJson(gson.toJson(value), JsonObject.class);
    }

    public JsonObject getValue() {
        return this.value;
    }
}
