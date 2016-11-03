package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JsonWriter {
    private Socket socket;

    public JsonWriter(Socket socket) {
        this.socket = socket;
    }

    public void writeJson(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        byte[] jsonBytes = json.getBytes(StandardCharsets.US_ASCII);
        ByteArrayWriter writer = new ByteArrayWriter(this.socket);
        writer.write(jsonBytes);
    }
}
