package edu.neumont.csc380.clientserver;

import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.Protocol;
import edu.neumont.csc380.clientserver.protocol.io.RequestWriter;
import edu.neumont.csc380.clientserver.protocol.io.ResponseReader;
import edu.neumont.csc380.clientserver.protocol.request.*;
import edu.neumont.csc380.clientserver.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private static Response makeRequest(Request request) {
        try (
                Socket connection = new Socket(Protocol.HOST, Protocol.PORT)
        ) {
            RequestWriter requestWriter = new RequestWriter(connection.getOutputStream());

            requestWriter.writeRequest(request);

            ResponseReader responseReader = new ResponseReader(connection.getInputStream());
            return responseReader.readResponse();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Response makeRequestUntilNotLocked(Request request) {
        Response response;
        do {
            response = makeRequest(request);
        } while (response.getType() == Response.Type.KEY_LOCKED);
        return response;
    }

    public static Response putObjectOnServer(String key, TypedObject value) {
        return makeRequest(new PutRequest(key, value));
    }

    public static TypedObject getObjectFromServer(String key) {
        Response response = makeRequestUntilNotLocked(new GetRequest(key));

        if (response.getType() == Response.Type.GET_SUCCESS) {
            GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

            return Protocol.deserializeTypedObject(responseWithValue.getValue());
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    public static Response updateObjectOnServer(String key, TypedObject value) {
        // Lock the item first
        makeRequestUntilNotLocked(new LockRequest(key));
        // Abort if lock not obtained, otherwise send update request
        return makeRequest(new UpdateRequest(key, value));
    }

    public static Response deleteObjectOnServer(String key) {
        return makeRequest(new DeleteRequest(key));
    }
}
