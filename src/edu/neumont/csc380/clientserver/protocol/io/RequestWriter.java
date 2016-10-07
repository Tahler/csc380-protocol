package edu.neumont.csc380.clientserver.protocol.io;

import com.google.gson.Gson;
import edu.neumont.csc380.clientserver.protocol.request.Request;

import java.io.OutputStream;

public class RequestWriter {
    private OutputStream outputStream;

    public RequestWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeRequest(Request request) {
        Gson gson = new Gson();
        String json = gson.toJson(request);

        StringWriter stringWriter = new StringWriter(this.outputStream);
        stringWriter.writeString(json);
    }
}
