package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonReader {
    private InputStream inputStream;

    public JsonReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String readJson() {
        Reader reader = new InputStreamReader(this.inputStream);
        try {
            reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        return gson.toJson(gson.fromJson(reader, JsonObject.class));
    }
}
