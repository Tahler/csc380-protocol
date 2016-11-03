package edu.neumont.csc380.clientserver.protocol.serialization;

import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.net.Socket;

public class ResponseWriter {
    private Socket socket;

    public ResponseWriter(Socket socket) {
        this.socket = socket;
    }

    public void writeResponse(Response response) {
        JsonWriter jsonWriter = new JsonWriter(this.socket);
        jsonWriter.writeJson(response);
    }
}
