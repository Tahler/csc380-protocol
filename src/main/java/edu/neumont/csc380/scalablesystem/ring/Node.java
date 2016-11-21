package edu.neumont.csc380.scalablesystem.ring;

import edu.neumont.csc380.scalablesystem.protocol.Protocol;
import edu.neumont.csc380.scalablesystem.protocol.request.Request;
import edu.neumont.csc380.scalablesystem.protocol.response.*;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestReader;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseWriter;
import edu.neumont.csc380.scalablesystem.repo.KeyDoesNotExistException;
import edu.neumont.csc380.scalablesystem.repo.LocalRepository;
import edu.neumont.csc380.scalablesystem.repo.RxHallaStor;
import rx.Completable;
import rx.Observable;
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
//        Request.Type operation = request.getType();
//
//        Observable<Response> response;
//        switch (operation) {
//            case CONTAINS_KEY:
//                response = this.responseForContainsKey(request.getKey());
//                break;
//            case GET:
//                response = this.responseForGet(request.getKey());
//                break;
//            case PUT:
//                PutRequest putRequest = (PutRequest) request;
//                response = this.responseForPut(putRequest.getKey(), putRequest.getValue());
//                break;
//            case UPDATE:
//                UpdateRequest updateRequest = (UpdateRequest) request;
//                response = this.responseForUpdate(updateRequest.getKey(), updateRequest.getValue());
//                break;
//            case DELETE:
//                response = this.responseForDelete(request.getKey());
//                break;
//            default:
//                throw new RuntimeException("Impossible request type: " + operation);
//        }
//        return response;
        return null;
    }

    private Single<Response> responseForContainsKey(String key) {
        return this.repository.containsKey(key)
                .map(ContainsKeySuccessResponse::new);
    }

    private Observable<Response> responseForPut(String key, Object value) {
//        // TODO: look up rxjava error handling
//        return this.repository.put(key, value)
//                .toObservable()
//                .map(val -> {
//
//                });
////                .toObservable()
////                .map(() -> new PutSuccessResponse())
//                .onErrorReturn(error -> {
//                    if (error instanceof KeyAlreadyExistsException) {
//                        return new KeyAlreadyExistsResponse();
////                    } else if (error instanceof) {
////
//                    } else {
//                        return new ServerFullResponse();
//                    }
//                });
//        Response response;
//        try {
//            this.repository.put(key, value);
//            response = new PutSuccessResponse();
//        } catch (KeyAlreadyExistsException e) {
//            response = new KeyAlreadyExistsResponse();
//        } catch (RepositoryFullException e) {
//            response = new ServerFullResponse();
//        }
//        return response;
        return null;
    }

    private Response responseForGet(String key) {
        Response response;
        try {
            Object value = this.repository.get(key);
            response = new GetSuccessResponse(value);
        } catch (KeyDoesNotExistException e) {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    private Response responseForUpdate(String key, Object value) {
        Response response;
        try {
            this.repository.update(key, value);
            response = new UpdateSuccessResponse();
        } catch (KeyDoesNotExistException e) {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    private Response responseForDelete(String key) {
        Response response;
        try {
            this.repository.delete(key);
            response = new DeleteSuccessResponse();
        } catch (KeyDoesNotExistException e) {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    public static void main(String[] args) {
        // TODO: parse arguments
        Node node = new Node(Protocol.HOST, Protocol.START_PORT);
        node.start();
    }
}

