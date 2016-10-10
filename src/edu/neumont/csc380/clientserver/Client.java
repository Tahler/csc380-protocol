package edu.neumont.csc380.clientserver;

import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.Protocol;
import edu.neumont.csc380.clientserver.protocol.io.RequestWriter;
import edu.neumont.csc380.clientserver.protocol.io.ResponseReader;
import edu.neumont.csc380.clientserver.protocol.request.*;
import edu.neumont.csc380.clientserver.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.clientserver.protocol.response.LockSuccessResponse;
import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final int MAX_WAIT_TIME = 500;

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
        long waitTime = 0;
        Response response;
        do {
            response = makeRequest(request);

            try {
                Thread.sleep(waitTime);
                if (waitTime <= MAX_WAIT_TIME) {
                    waitTime += 10;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (response.getType() == Response.Type.KEY_LOCKED);
        return response;
    }

    public static Response putObjectOnServer(String key, TypedObject value) {
        return makeRequest(new PutRequest(key, value));
    }

    public static TypedObject getObjectOnServer(String key) {
        Response response = makeRequestUntilNotLocked(new GetRequest(key));

        if (response.getType() == Response.Type.GET_SUCCESS) {
            GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

            return Protocol.deserializeTypedObject(responseWithValue.getValue());
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    public static TypedObject getAndLockObjectOnServer(String key) {
        Response response = makeRequestUntilNotLocked(new LockRequest(key));

        if (response.getType() == Response.Type.LOCK_SUCCESS) {
            LockSuccessResponse responseWithValue = (LockSuccessResponse) response;

            return Protocol.deserializeTypedObject(responseWithValue.getValue());
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    public static Response updateObjectOnServer(String key, TypedObject value) {
        return makeRequest(new UpdateRequest(key, value));
    }

    public static Response deleteObjectOnServer(String key) {
        return makeRequest(new DeleteRequest(key));
    }
}
