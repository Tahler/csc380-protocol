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
    private Response makeRequest(Request request) {
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

    private Response makeRequestUntilNotLocked(Request request) {
        Response response = null;
        do {
//            if (response != null) {
//                // TODO: dumb
////                try {
////                    Thread.sleep(100);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }
            response = this.makeRequest(request);
        } while (response.getType() == Response.Type.KEY_LOCKED);
        return response;
    }

    public Response putObjectOnServer(String key, TypedObject value) {
        return this.makeRequest(new PutRequest(key, value));
    }

    public TypedObject getObjectFromServer(String key) {
        Response response = this.makeRequestUntilNotLocked(new GetRequest(key));

        if (response.getType() == Response.Type.GET_SUCCESS) {
            GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

            return Protocol.deserializeTypedObject(responseWithValue.getValue());
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    public Response updateObjectOnServer(String key, TypedObject value) {
        // Lock the item first
        this.makeRequestUntilNotLocked(new LockRequest(key));
        // Abort if lock not obtained, otherwise send update request
        return this.makeRequest(new UpdateRequest(key, value));
    }

    public Response deleteObjectOnServer(String key) {
        return this.makeRequest(new DeleteRequest(key));
    }
}
