package edu.neumont.csc380.clientserver.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.neumont.csc380.clientserver.models.Driver;
import edu.neumont.csc380.clientserver.models.Racecar;
import edu.neumont.csc380.clientserver.models.TypedObject;

public class Protocol {
    public static final String HOST = "localhost";
    public static final int PORT = 3000;

    public static final char ESCAPE_CHARACTER = '\\';
    public static final char STRING_TERMINATOR = ';';

    public static TypedObject deserializeTypedObject(JsonObject value) {
        String jsonDataType = value.get("type").getAsString();
        TypedObject.Type dataType = TypedObject.Type.valueOf(jsonDataType);

        JsonObject dataJson = value.get("data").getAsJsonObject();

        Gson gson = new Gson();
        TypedObject deserialized;
        switch (dataType) {
            case DRIVER:
                Driver driver = gson.fromJson(dataJson, Driver.class);
                deserialized = new TypedObject<>(dataType, driver);
                break;
            case RACECAR:
                Racecar racecar = gson.fromJson(dataJson, Racecar.class);
                deserialized = new TypedObject<>(dataType, racecar);
                break;
            default:
                throw new RuntimeException("Impossible data type: " + dataType);
        }
        return deserialized;
    }

}
