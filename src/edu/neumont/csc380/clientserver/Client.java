package edu.neumont.csc380.clientserver;

import edu.neumont.csc380.clientserver.models.Driver;
import edu.neumont.csc380.clientserver.models.Racecar;
import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.Protocol;
import edu.neumont.csc380.clientserver.protocol.io.RequestWriter;
import edu.neumont.csc380.clientserver.protocol.io.ResponseReader;
import edu.neumont.csc380.clientserver.protocol.request.*;
import edu.neumont.csc380.clientserver.protocol.response.GetSuccessResponse;
import edu.neumont.csc380.clientserver.protocol.response.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Client {
    private Set<String> keys;

    public Client() {
        this.keys = new HashSet<>();
    }

    /**
     * @return The number of items in the HallaStor.
     */
    public int fillServer() {
        Random random = new Random();
        TypedObject driver = new TypedObject<>(TypedObject.Type.DRIVER, new Driver(123, "Mike", 40, true));
        TypedObject racecar = new TypedObject<>(TypedObject.Type.RACECAR, new Racecar(12, "Porsche", "Cayman", 35000, 60));

        int i = 0;
        Response.Type responseType;
        do {
            String key = i + "";
            TypedObject value = random.nextBoolean() ? driver : racecar;

            Response response = this.putObjectOnServer(key, value);

            responseType = response.getType();
            if (responseType == Response.Type.PUT_SUCCESS) {
                i += 1;
                this.keys.add(key);
            }
        } while (responseType == Response.Type.PUT_SUCCESS); // responseType != Response.Type.SERVER_FULL);

        return i;
    }

    public long timeUpdates(String keyToUpdate, int numUpdates) {
        long totalTime = 0;

        for (int i = 0; i < numUpdates; i++) {
            long startTime = System.currentTimeMillis();

            TypedObject value = this.getObjectFromServer(keyToUpdate);
            switch (value.getType()) {
                case DRIVER:
                    Driver driver = (Driver) value.getData();
                    driver.setAge(driver.getAge() + 1);
                    break;
                case RACECAR:
                    Racecar racecar = (Racecar) value.getData();
                    racecar.setHorsePower(racecar.getHorsePower() + 1);
                    break;
                default:
                    throw new RuntimeException("Impossible type: " + value.getType());
            }
            // Ignoring response
            this.updateObjectOnServer(keyToUpdate, value);

            long endTime = System.currentTimeMillis();
            totalTime += endTime - startTime;
        }
        return totalTime;
    }

    private Response makeRequest(Request request) {
        try (
                Socket connection = new Socket(Protocol.HOST, Protocol.PORT)
        ) {
            RequestWriter requestWriter = new RequestWriter(connection.getOutputStream());

            requestWriter.writeRequest(request);

            ResponseReader responseReader = new ResponseReader(connection.getInputStream());
            return responseReader.readResponse();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Response putObjectOnServer(String key, TypedObject value) {
        return this.makeRequest(new PutRequest(key, value));
    }

    public TypedObject getObjectFromServer(String key) {
        Response response = this.makeRequest(new GetRequest(key));

        if (response.getType() == Response.Type.GET_SUCCESS) {
            GetSuccessResponse responseWithValue = (GetSuccessResponse) response;

            return Protocol.deserializeTypedObject(responseWithValue.getValue());
        } else {
            throw new RuntimeException("Server returned bad response: " + response.getType());
        }
    }

    public Response updateObjectOnServer(String keyToUpdate, TypedObject value) {
        return this.makeRequest(new UpdateRequest(keyToUpdate, value));
    }

    public Response deleteObjectOnServer(String key) {
        return this.makeRequest(new DeleteRequest(key));
    }

    public static void main(String[] args) {
        final int numUpdates = 100;

        Client client = new Client();

        int numItems = client.fillServer();
        System.out.println("Filled server with " + numItems + " items.");

        String key = (numItems - 1) + "";

        long time = client.timeUpdates(key, numUpdates);

        double seconds = time / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + time + " millis (" + seconds + " seconds).");
    }
}
