package edu.neumont.csc380.scalablesystem.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.neumont.csc380.scalablesystem.models.Driver;
import edu.neumont.csc380.scalablesystem.models.Racecar;
import edu.neumont.csc380.scalablesystem.models.TypedObject;

public class Protocol {
    public static final String HOST = "localhost";
    public static final int START_PORT = 3000;

    public static TypedObject deserializeTypedObject(Object value) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(gson.toJson(value), JsonObject.class);

        String jsonDataType = jsonObject.get("type").getAsString();
        TypedObject.Type dataType = TypedObject.Type.valueOf(jsonDataType);

        JsonObject dataJson = jsonObject.get("data").getAsJsonObject();

        TypedObject deserialized;
        switch (dataType) {
            case DRIVER:
                Driver driver = gson.fromJson(dataJson, Driver.class);
                deserialized = new TypedObject(dataType, driver);
                break;
            case RACECAR:
                Racecar racecar = gson.fromJson(dataJson, Racecar.class);
                deserialized = new TypedObject(dataType, racecar);
                break;
            default:
                throw new RuntimeException("Impossible data type: " + dataType);
        }
        return deserialized;
    }

}
