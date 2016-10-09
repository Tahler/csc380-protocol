package edu.neumont.csc380.clientserver;

import edu.neumont.csc380.clientserver.models.Driver;
import edu.neumont.csc380.clientserver.models.Racecar;
import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.response.Response;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Clients {
    private List<String> keys;

    public Clients() {
        this.keys = new ArrayList<>();
    }

    /**
     * @return The number of items in the HallaStor.
     */
    public int fillServer() {
        Random random = new Random();
        final TypedObject driver = new TypedObject<>(TypedObject.Type.DRIVER, new Driver(123, "Mike", 40, true));
        final TypedObject racecar = new TypedObject<>(TypedObject.Type.RACECAR, new Racecar(12, "Porsche", "Cayman", 35000, 60));

        int i = 0;
        Response.Type responseType;
        do {
            Client client = new Client();

            String key = i + "";
            TypedObject value = random.nextBoolean() ? driver : racecar;

            Response response = client.putObjectOnServer(key, value);

            responseType = response.getType();
            if (responseType == Response.Type.PUT_SUCCESS) {
                i += 1;
                this.keys.add(key);
            }
        } while (responseType == Response.Type.PUT_SUCCESS); // responseType != Response.Type.SERVER_FULL);

        return i;
    }

    public void performUpdates(final int numUpdates) {
        List<Observable<Void>> updates = new ArrayList<>(numUpdates);

        for (int i = 0; i < numUpdates; i++) {
            final int ii = i;

            Observable<Void> update = Observable.create(subscriber -> {
                new Thread(() -> {
                    System.out.println(ii + " started.");

                    this.updateRandomKey();

                    System.out.println(ii + " done.");
                    // Finish
                    subscriber.onCompleted();
                }).start();
            });
            updates.add(update);
        }
        Observable.merge(updates).toBlocking().subscribe();
    }

    public void updateRandomKey() {
        Random random = new Random();
        int randomIndex = random.nextInt(this.keys.size());
        String randomKey = this.keys.get(randomIndex);

        Client client = new Client();
        TypedObject value = client.getObjectFromServer(randomKey);
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
        client.updateObjectOnServer(randomKey, value);
    }

    public static void main(String[] args) {
        final int numUpdates = 100;

        Clients clients = new Clients();

        int numItems = clients.fillServer();
        System.out.println("Filled server with " + numItems + " items.");

        long startTime = System.currentTimeMillis();
        clients.performUpdates(numUpdates);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");
    }
}
