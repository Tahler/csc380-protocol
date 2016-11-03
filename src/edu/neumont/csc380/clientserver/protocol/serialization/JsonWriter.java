package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JsonWriter {
    private OutputStream outputStream;

    public JsonWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeJson(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        byte[] jsonBytes = json.getBytes(StandardCharsets.US_ASCII);
        ByteArrayWriter writer = new ByteArrayWriter(this.outputStream);
        writer.write(jsonBytes);
    }
}
