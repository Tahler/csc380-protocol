package edu.neumont.csc380.clientserver.protocol.serialization;

import edu.neumont.csc380.clientserver.protocol.request.Request;

import java.net.Socket;

public class RequestWriter {
    private Socket socket;

    public RequestWriter(Socket socket) {
        this.socket = socket;
    }

    public void writeRequest(Request request) {
        JsonWriter jsonWriter = new JsonWriter(this.socket);
        jsonWriter.writeJson(request);
    }
}
