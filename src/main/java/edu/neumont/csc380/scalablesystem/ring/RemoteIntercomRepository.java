package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.protocol.request.*;
import edu.neumont.csc380.scalablesystem.protocol.response.ContainsKeySuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.scalablesystem.protocol.response.Response;
import edu.neumont.csc380.scalablesystem.ring.repo.RepositoryFullException;
import edu.neumont.csc380.scalablesystem.ring.repo.RxHallaStor;
import rx.Completable;
import rx.Single;

import java.io.IOException;
import java.net.Socket;

// Lots of copied code from RemoteRepository
public class RemoteIntercomRepository implements RxHallaStor {
    public final RingNodeInfo remoteNodeInfo;

    public RemoteIntercomRepository(RingNodeInfo remoteNodeInfo) {
        this.remoteNodeInfo = remoteNodeInfo;
    }

    @Override
    public Single<Boolean> containsKey(String key) {
        return this.makeIntercomRequest(new ContainsKeyRequest(key))
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
        Env.LOGGER.debug("Intercom (" + this.remoteNodeInfo.intercomPort + ") : putting " + key + " : " + value);

        return this.makeIntercomRequest(new PutRequest(key, value))
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
        return this.makeIntercomRequest(new GetRequest(key))
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
        return this.makeIntercomRequest(new UpdateRequest(key, value))
                .doOnSuccess(response -> {
                    if (response.getType() != Response.Type.UPDATE_SUCCESS) {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                })
                .toCompletable();
    }

    @Override
    public Completable delete(String key) {
        return this.makeIntercomRequest(new DeleteRequest(key))
                .doOnSuccess(response -> {
                    if (response.getType() != Response.Type.DELETE_SUCCESS) {
                        throw new RuntimeException("Server returned bad response: " + response.getType());
                    }
                })
                .toCompletable();
    }

    protected Single<Response> makeIntercomRequest(Request request) {
        Socket connection = getSocket(this.remoteNodeInfo.host, this.remoteNodeInfo.intercomPort);
        return RemoteRepository.writeRequest(connection, request);
    }

    @Override
    public String toString() {
        return "RemoteIntercomRepository{" +
                "remoteNodeInfo=" + remoteNodeInfo +
                '}';
    }

    private static Socket getSocket(String host, int port) {
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
