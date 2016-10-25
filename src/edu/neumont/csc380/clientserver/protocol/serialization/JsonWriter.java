package edu.neumont.csc380.clientserver.protocol.serialization;

import com.google.gson.Gson;
import com.hallaLib.HallaZip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class JsonWriter {
    private OutputStream outputStream;

    public JsonWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeJson(Object object) {
        Writer writer = new OutputStreamWriter(this.outputStream);

        Gson gson = new Gson();
        String json = gson.toJson(object);
        try {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
