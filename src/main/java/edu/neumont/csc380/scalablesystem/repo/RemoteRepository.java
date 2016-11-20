package edu.neumont.csc380.scalablesystem.repo;

import edu.neumont.csc380.scalablesystem.protocol.checksum.NonEqualChecksumException;
import edu.neumont.csc380.scalablesystem.protocol.request.*;
import edu.neumont.csc380.scalablesystem.protocol.response.ContainsKeySuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.Response;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestWriter;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseReader;

import java.io.IOException;
import java.net.Socket;

public class RemoteRepository implements Repository<String, Object> {
    private static final int MAX_WAIT_TIME = 500;

    private final String host;
    private final int port;

    public RemoteRepository(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean containsKey(String key) {
        Response response = this.makeRequest(new ContainsKeyRequest(key));

        boolean containsKey;
        if (response.getType() == Response.Type.CONTAINS_KEY_SUCCESS) {
            ContainsKeySuccessResponse containsKeySuccessResponse = (ContainsKeySuccessResponse) response;
            containsKey = containsKeySuccessResponse.containsKey();
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
        return containsKey;
    }

    @Override
    public void put(String key, Object value) {
        Response response = this.makeRequest(new PutRequest(key, value));

        Response.Type responseType = response.getType();

        if (responseType != Response.Type.PUT_SUCCESS) {
            if (responseType == Response.Type.SERVER_FULL) {
                throw new RepositoryFullException();
            } else {
                throw new RuntimeException("Server returned bad response: " + response.getType());
            }
        }
    }

    @Override
    public Object get(String key) {
        Response response = this.makeRequest(new GetRequest(key));

        Object value;
        if (response.getType() == Response.Type.GET_SUCCESS) {
            GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

            value = responseWithValue.getValue();
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
        return value;
    }

    @Override
    public void update(String key, Object value) {
        Response response = this.makeRequest(new UpdateRequest(key, value));

        if (response.getType() != Response.Type.UPDATE_SUCCESS) {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    @Override
    public void delete(String key) {
        Response response = this.makeRequest(new DeleteRequest(key));

        if (response.getType() != Response.Type.DELETE_SUCCESS) {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    private Response makeRequest(Request request) {
        try (
                Socket connection = new Socket(this.host, this.port)
        ) {
            RequestWriter requestWriter = new RequestWriter(connection);

            requestWriter.writeRequest(request);

            ResponseReader responseReader = new ResponseReader(connection);
            Response response = null;
            do {
                try {
                    response = responseReader.readResponse();
                    // send 0
                } catch (NonEqualChecksumException e) {
                    e.printStackTrace();
                    System.out.println("Retrying...");
                    // send 1
                }
            } while (response == null);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
