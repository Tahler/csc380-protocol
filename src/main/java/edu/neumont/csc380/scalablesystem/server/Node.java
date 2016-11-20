package edu.neumont.csc380.scalablesystem.server;

import edu.neumont.csc380.scalablesystem.repo.*;
import edu.neumont.csc380.scalablesystem.protocol.Protocol;
import edu.neumont.csc380.scalablesystem.protocol.serialization.RequestReader;
import edu.neumont.csc380.scalablesystem.protocol.serialization.ResponseWriter;
import edu.neumont.csc380.scalablesystem.protocol.request.PutRequest;
import edu.neumont.csc380.scalablesystem.protocol.request.Request;
import edu.neumont.csc380.scalablesystem.protocol.request.UpdateRequest;
import edu.neumont.csc380.scalablesystem.protocol.response.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Node {
    private final int port;
    private boolean running;
    private final Repository<String, Object> repository;

    public Node(int port) {
        this.port = port;
        this.running = false;
        this.repository = new PhysicalRepository();
    }

    public void start() {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(this.port);
                System.out.println("Listening on port " + this.port);

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

    private void handleClient(Socket client) {
        RequestReader requestReader = new RequestReader(client);
        Request request = requestReader.readRequest();
        Response response = this.handleRequest(request);

        ResponseWriter responseWriter = new ResponseWriter(client);
        responseWriter.writeResponse(response);
    }

    private Response handleRequest(Request request) {
        Request.Type operation = request.getType();

        Response response;
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

    private Response responseForContainsKey(String key) {
        boolean containsKey = this.repository.containsKey(key);
        return new ContainsKeySuccessResponse(containsKey);
    }

    private Response responseForPut(String key, Object value) {
        Response response;
        try {
            this.repository.put(key, value);
            response = new PutSuccessResponse();
        } catch (KeyAlreadyExistsException e) {
            response = new KeyAlreadyExistsResponse();
        } catch (RepositoryFullException e) {
            response = new ServerFullResponse();
        }
        return response;
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
        Node node = new Node(Protocol.PORT);
        node.start();
    }
}

