package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonReader {
    private InputStream inputStream;

    public JsonReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String readJson() {
        ByteArrayReader byteArrayReader = new ByteArrayReader(this.inputStream);
        byte[] bytes = byteArrayReader.read();
        String converted = new String(bytes, StandardCharsets.US_ASCII);

        System.out.println(converted);
        Gson gson = new Gson();
        return gson.toJson(gson.fromJson(converted, JsonObject.class));
    }
}
