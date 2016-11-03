package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;
import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.io.OutputStream;

public class ResponseWriter {
    private OutputStream outputStream;

    public ResponseWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeResponse(Response response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);

        StringWriter stringWriter = new StringWriter(this.outputStream);
        stringWriter.writeString(json);
    }
}
