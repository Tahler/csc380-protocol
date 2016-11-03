package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JsonReader {
    private Socket socket;

    public JsonReader(Socket socket) {
        this.socket = socket;
    }

    public String readJson() {
        ByteArrayReader byteArrayReader = new ByteArrayReader(this.socket);
        byte[] bytes = byteArrayReader.read();
        String converted = new String(bytes, StandardCharsets.US_ASCII);

        Gson gson = new Gson();
        return gson.toJson(gson.fromJson(converted, JsonObject.class));
    }
}
