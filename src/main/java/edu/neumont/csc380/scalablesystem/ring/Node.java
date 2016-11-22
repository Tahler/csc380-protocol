package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.protocol.Protocol;
import edu.neumont.csc380.scalablesystem.protocol.request.PutRequest;
import edu.neumont.csc380.scalablesystem.protocol.request.Request;
import edu.neumont.csc380.scalablesystem.protocol.request.UpdateRequest;
import edu.neumont.csc380.scalablesystem.protocol.response.*;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestReader;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseWriter;
import edu.neumont.csc380.scalablesystem.repo.*;
import rx.Completable;
import rx.Single;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Node {
    private final RingNodeInfo info;
    private final RxHallaStor repository;
    private boolean running;

    public Node(String host, int port) {
        this.info = new RingNodeInfo(host, port);
        this.repository = new RingCoordinator(this.info, new LocalRepository(), new RingInfo());
        this.running = false;
    }

    public void start() {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(this.info.port);
                System.out.println("Listening on port " + this.info.port);

                this.running = true;
                while (this.running) {
                    Socket client = server.accept();

                    Thread handler = new Thread(() -> this.handle(client));
                    handler.start();
                }
            } catch (IOException e) {
                System.err.println("Failed to start server on port 3000.");
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        this.running = false;
    }

    private Completable handle(Socket client) {
        RequestReader requestReader = new RequestReader(client);
        Request request = requestReader.readRequest();
        return this.handleRequest(request)
                .doOnSuccess(response -> {
                    ResponseWriter responseWriter = new ResponseWriter(client);
                    responseWriter.writeResponse(response);
                })
                .toCompletable();
    }

    private Single<Response> handleRequest(Request request) {
        Request.Type operation = request.getType();

        Single<Response> response;
        switch (operation) {
            case CONTAINS_KEY:
                response = this.responseForContainsKey(request.getKey());
                break;
            case GET:
                response = this.responseForGet(request.getKey());
                break;
            case PUT:
                PutRequest putRequest = (PutRequest) request;
                response = this.responseForPut(putRequest.getKey(), putRequest.getValue());
                break;
            case UPDATE:
                UpdateRequest updateRequest = (UpdateRequest) request;
                response = this.responseForUpdate(updateRequest.getKey(), updateRequest.getValue());
                break;
            case DELETE:
                response = this.responseForDelete(request.getKey());
                break;
            default:
                throw new RuntimeException("Impossible request type: " + operation);
        }
        return response;
    }

    private Single<Response> responseForContainsKey(String key) {
        return this.repository.containsKey(key)
                .map(ContainsKeySuccessResponse::new);
    }

    private Single<Response> responseForPut(String key, Object value) {
        return Single.create(subscriber -> {
            this.repository.put(key, value)
                    .subscribe(
                            () -> subscriber.onSuccess(new PutSuccessResponse()),
                            err -> {
                                if (err instanceof KeyAlreadyExistsException) {
                                    subscriber.onSuccess(new KeyAlreadyExistsResponse());
                                } else if (err instanceof RepositoryFullException) {
                                    subscriber.onSuccess(new ServerFullResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForGet(String key) {
        return Single.create(subscriber -> {
            this.repository.get(key)
                    .subscribe(
                            val -> subscriber.onSuccess(new GetSuccessResponse(val)),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForUpdate(String key, Object value) {
        return Single.create(subscriber -> {
            this.repository.update(key, value)
                    .subscribe(
                            () -> subscriber.onSuccess(new UpdateSuccessResponse()),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    private Single<Response> responseForDelete(String key) {
        return Single.create(subscriber -> {
            this.repository.delete(key)
                    .subscribe(
                            () -> subscriber.onSuccess(new DeleteSuccessResponse()),
                            err -> {
                                if (err instanceof KeyDoesNotExistException) {
                                    subscriber.onSuccess(new KeyDoesNotExistResponse());
                                } else {
                                    subscriber.onError(err);
                                }
                            });
        });
    }

    public static void main(String[] args) {
        // TODO: parse arguments
        Node node = new Node(Protocol.HOST, Protocol.START_PORT);
        node.start();
    }
}

