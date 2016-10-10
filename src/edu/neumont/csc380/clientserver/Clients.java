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
            String key = i + "";
            TypedObject value = random.nextBoolean() ? driver : racecar;

            Response response = Client.putObjectOnServer(key, value);

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
            Observable<Void> update = Observable.create(subscriber ->
                new Thread(() -> {
                    String key = "0";
//                    String key = this.getRandomKey();
                    this.updateKey(key);

                    subscriber.onCompleted();
                }).start());
            updates.add(update);
        }
        Observable.merge(updates).toBlocking().subscribe();
    }

    private String getRandomKey() {
        Random random = new Random();
        int randomIndex = random.nextInt(this.keys.size());
        return this.keys.get(randomIndex);
    }

    public void updateKey(String key) {
        TypedObject value = Client.getAndLockObjectOnServer(key);

        Driver driver = (Driver) value.getData();
        driver.setAge(driver.getAge() + 1);

        Client.updateObjectOnServer(key, value);
    }

    public static void main(String[] args) {
        final int numUpdates = 100;

        Client.putObjectOnServer("0", new TypedObject<>(TypedObject.Type.DRIVER, new Driver(123, "Mike", 0, true)));

        long startTime = System.currentTimeMillis();
        Clients clients = new Clients();
        clients.performUpdates(numUpdates);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        TypedObject object = Client.getObjectOnServer("0");
        Driver driver = (Driver) object.getData();
        System.out.println(driver.getAge());

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");
    }
}

// TODO:
// - Logger
// - Fully utilize Rx
