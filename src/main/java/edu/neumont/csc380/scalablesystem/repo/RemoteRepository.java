package edu.neumont.csc380.scalablesystem.repo;

import edu.neumont.csc380.scalablesystem.protocol.checksum.NonEqualChecksumException;
import edu.neumont.csc380.scalablesystem.protocol.request.*;
import edu.neumont.csc380.scalablesystem.protocol.response.ContainsKeySuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.Response;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestWriter;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseReader;
import edu.neumont.csc380.scalablesystem.ring.RingNodeInfo;
import rx.Completable;
import rx.Single;

import java.io.IOException;
import java.net.Socket;

public class RemoteRepository implements RxHallaStor {
    private final RingNodeInfo remoteNodeInfo;

    public RemoteRepository(RingNodeInfo remoteNodeInfo) {
        this.remoteNodeInfo = remoteNodeInfo;
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        return this.makeRequest(new ContainsKeyRequest(key))
                .map(response -> {
                    boolean containsKey;
                    if (response.getType() == Response.Type.CONTAINS_KEY_SUCCESS) {
                        ContainsKeySuccessResponse containsKeySuccessResponse = (ContainsKeySuccessResponse) response;
                        containsKey = containsKeySuccessResponse.containsKey();
                    } else {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                    return containsKey;
                });
    }

    @Override
    public Completable put(String key, Object value) {
        return this.makeRequest(new PutRequest(key, value))
                .doOnSuccess(response -> {
                    Response.Type responseType = response.getType();

                    if (responseType != Response.Type.PUT_SUCCESS) {
                        if (responseType == Response.Type.SERVER_FULL) {
                            throw new RepositoryFullException();
                        } else {
                            throw new RuntimeException("Server returned bad response: " + response.getType());
                        }
                    }
                })
                .toCompletable();
    }

    @Override
    public Single<Object> get(String key) {
        return this.makeRequest(new GetRequest(key))
                .map(response -> {
                    Object value;
                    if (response.getType() == Response.Type.GET_SUCCESS) {
                        GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

                        value = responseWithValue.getValue();
                    } else {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                    return value;
                });
    }

    @Override
    public Completable update(String key, Object value) {
        return this.makeRequest(new UpdateRequest(key, value))
                .doOnSuccess(response -> {
                    if (response.getType() != Response.Type.UPDATE_SUCCESS) {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                })
                .toCompletable();
    }

    @Override
    public Completable delete(String key) {
        return this.makeRequest(new DeleteRequest(key))
                .doOnSuccess(response -> {
                    if (response.getType() != Response.Type.DELETE_SUCCESS) {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                })
                .toCompletable();
    }

    private Single<Response> makeRequest(Request request) {
        return Single.create(subscriber -> {
            try (
                    Socket connection = new Socket(this.remoteNodeInfo.host, this.remoteNodeInfo.port)
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

                subscriber.onSuccess(response);
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        });
    }
}
