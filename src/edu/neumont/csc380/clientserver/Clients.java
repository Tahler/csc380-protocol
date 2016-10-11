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
                    String key = this.getRandomKey();
                    this.updateKey(key);

                    subscriber.onCompleted();
                }).start());
            updates.add(update);
        }
        Observable.merge(updates).toBlocking().subscribe();
    }

    public void performUpdatesOnKey(final int numUpdates, final String key) {
        List<Observable<Void>> updates = new ArrayList<>(numUpdates);

        for (int i = 0; i < numUpdates; i++) {
            Observable<Void> update = Observable.create(subscriber ->
                    new Thread(() -> {
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
        switch (value.getType()) {
            case DRIVER:
                Driver driver = (Driver) value.getData();
                driver.setAge(driver.getAge() + 1);
                break;
            case RACECAR:
                Racecar racecar = (Racecar) value.getData();
                racecar.setHorsePower(racecar.getHorsePower() + 1);
                break;
        }
        Client.updateObjectOnServer(key, value);
    }
}

// TODO:
// - Logger
// - Fully utilize Rx
