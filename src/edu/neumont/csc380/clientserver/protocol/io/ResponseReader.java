package edu.neumont.csc380.clientserver.protocol.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.neumont.csc380.clientserver.protocol.response.*;

import java.io.InputStream;

public class ResponseReader {
    private InputStream inputStream;

    public ResponseReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Response readResponse() {
        StringReader stringReader = new StringReader(this.inputStream);
        String json = stringReader.readString();

        Response.Type responseType = parseType(json);
        return this.deserializeBasedOnTypeParameter(json, responseType);
    }

    private Response deserializeBasedOnTypeParameter(String json, Response.Type responseType) {
        Class<? extends Response> subClass;
        switch (responseType) {
            case GET_SUCCESS:
                subClass = GetSuccessResponse.class;
                break;
            case PUT_SUCCESS:
                subClass = PutSuccessResponse.class;
                break;
            case UPDATE_SUCCESS:
                subClass = UpdateSuccessResponse.class;
                break;
            case DELETE_SUCCESS:
                subClass = DeleteSuccessResponse.class;
                break;
            case INVALID_REQUEST:
                subClass = InvalidRequestResponse.class;
                break;
            case INVALID_KEY:
                subClass = InvalidKeyResponse.class;
                break;
            case KEY_DOES_NOT_EXIST:
                subClass = KeyDoesNotExistResponse.class;
                break;
            case KEY_ALREADY_EXISTS:
                subClass = KeyAlreadyExistsResponse.class;
                break;
            case SERVER_FULL:
                subClass = ServerFullResponse.class;
                break;
            default:
                throw new RuntimeException("Impossible response type: " + responseType);
        }

        Gson gson = new Gson();
        return gson.fromJson(json, subClass);
    }

    private static Response.Type parseType(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        String jsonType = jsonObject.get("type").getAsString();
        return Response.Type.valueOf(jsonType);
    }
}
