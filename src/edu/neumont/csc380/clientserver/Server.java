package edu.neumont.csc380.clientserver;

import com.google.gson.JsonObject;
import com.hallaLib.HallaStor;
import edu.neumont.csc380.clientserver.models.RepositoryItem;
import edu.neumont.csc380.clientserver.protocol.Protocol;
import edu.neumont.csc380.clientserver.protocol.io.RequestReader;
import edu.neumont.csc380.clientserver.protocol.io.ResponseWriter;
import edu.neumont.csc380.clientserver.protocol.request.PutRequest;
import edu.neumont.csc380.clientserver.protocol.request.Request;
import edu.neumont.csc380.clientserver.protocol.request.UpdateRequest;
import edu.neumont.csc380.clientserver.protocol.response.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private boolean running;
    private boolean shouldLock;
    private HallaStor repository;

    public Server() {
        this(true);
    }

    public Server(boolean shouldLock) {
        this.running = false;
        this.shouldLock = shouldLock;
        this.repository = HallaStor.getInstance();
    }

    public void start() {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(Protocol.PORT);
                System.out.println("Listening on port " + Protocol.PORT);

                this.running = true;
                while (this.running) {
                    Socket client = server.accept();

                    Thread handler = new Thread(() -> this.handleClient(client));
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

    private RepositoryItem getItem(String key) {
        return (RepositoryItem) this.repository.get(key);
    }

    private void handleClient(Socket client) {
        try {
            RequestReader requestReader = new RequestReader(client.getInputStream());
            Request request = requestReader.readRequest();

            Response response = this.handleRequest(request);

            ResponseWriter responseWriter = new ResponseWriter(client.getOutputStream());
            responseWriter.writeResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response handleRequest(Request request) {
        Request.Type operation = request.getType();

        Response response;
        switch (operation) {
            case GET:
                response = this.responseForGet(request.getKey());
                break;
            case PUT:
                PutRequest putRequest = (PutRequest) request;
                response = this.responseForPut(putRequest.getKey(), putRequest.getValue());
                break;
            case LOCK:
                response = this.responseForLock(request.getKey());
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

    private Response responseForGet(String key) {
        Response response;
        if (this.repository.containsKey(key)) {
            RepositoryItem item = this.getItem(key);
            if (shouldLock && item.isLocked()) {
                response = new KeyIsLockedResponse();
            } else {
                JsonObject value = (JsonObject) item.getValue();
                response = new GetSuccessResponse(value);
            }
        } else {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    private Response responseForPut(String key, Object value) {
        Response response;
        if (this.repository.containsKey(key)) {
            response = new KeyAlreadyExistsResponse();
        } else {
            try {
                RepositoryItem item = new RepositoryItem(value);
                this.repository.add(key, item);
                response = new PutSuccessResponse();
            } catch (Exception e) {
                response = new ServerFullResponse();
            }
        }
        return response;
    }

    private Response responseForLock(String key) {
        Response response;
        if (this.repository.containsKey(key)) {
            RepositoryItem item = this.getItem(key);
            if (shouldLock && item.isLocked()) {
                response = new KeyIsLockedResponse();
            } else {
                item.lock();
                JsonObject value = (JsonObject) item.getValue();
                response = new LockSuccessResponse(value);
            }
        } else {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    private Response responseForUpdate(String key, Object value) {
        Response response;
        if (this.repository.containsKey(key)) {
            RepositoryItem item = this.getItem(key);
            if (!shouldLock || item.isLocked()) {
                item.setValue(value);
                this.repository.update(key, item);
                item.unlock();
                response = new UpdateSuccessResponse();
            } else {
                response = new KeyNotLockedResponse();
            }
        } else {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    private Response responseForDelete(String key) {
        Response response;
        if (this.repository.containsKey(key)) {
            RepositoryItem item = this.getItem(key);
            if (!shouldLock || item.isLocked()) {
                this.repository.delete(key);
                response = new DeleteSuccessResponse();
            } else {
                response = new KeyNotLockedResponse();
            }
        } else {
            response = new KeyDoesNotExistResponse();
        }
        return response;
    }

    public static void main(String[] args) {
        final boolean shouldLock = true;

        Server server = new Server(shouldLock);
        server.start();
    }
}

