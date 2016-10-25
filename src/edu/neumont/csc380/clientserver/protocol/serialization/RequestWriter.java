package edu.neumont.csc380.clientserver.protocol.serialization;

import edu.neumont.csc380.clientserver.protocol.request.Request;

import java.io.OutputStream;

public class RequestWriter {
    private OutputStream outputStream;

    public RequestWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeRequest(Request request) {
        JsonWriter jsonWriter = new JsonWriter(this.outputStream);
        jsonWriter.writeJson(request);
    }
}
