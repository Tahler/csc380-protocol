package edu.neumont.csc380.scalablesystem.protocol.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.neumont.csc380.scalablesystem.protocol.request.*;

import java.net.Socket;

public class RequestReader {
    private Socket socket;

    public RequestReader(Socket socket) {
        this.socket = socket;
    }

    public Request readRequest() {
        JsonReader jsonReader = new JsonReader(this.socket);
        String json = jsonReader.readJson();
        Request.Type requestType = parseType(json);
        return this.deserializeBasedOnTypeParameter(json, requestType);
    }

    private Request deserializeBasedOnTypeParameter(String json, Request.Type requestType) {
        Class<? extends Request> subClass;
        switch (requestType) {
            case GET:
                subClass = GetRequest.class;
                break;
            case PUT:
                subClass = PutRequest.class;
                break;
            case UPDATE:
                subClass = UpdateRequest.class;
                break;
            case DELETE:
                subClass = DeleteRequest.class;
                break;
            default:
                throw new RuntimeException("Impossible value in request type.");
        }

        Gson gson = new Gson();
        return gson.fromJson(json, subClass);
    }

    private static Request.Type parseType(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        String jsonType = jsonObject.get("type").getAsString();
        return Request.Type.valueOf(jsonType);
    }
}
