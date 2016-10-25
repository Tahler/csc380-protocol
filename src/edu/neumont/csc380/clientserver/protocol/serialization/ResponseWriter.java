package edu.neumont.csc380.clientserver.protocol.serialization;

import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.io.OutputStream;

public class ResponseWriter {
    private OutputStream outputStream;

    public ResponseWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeResponse(Response response) {
        JsonWriter jsonWriter = new JsonWriter(this.outputStream);
        jsonWriter.writeJson(response);
    }
}
