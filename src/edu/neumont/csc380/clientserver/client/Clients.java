package edu.neumont.csc380.clientserver.client;

import edu.neumont.csc380.clientserver.models.Driver;
import edu.neumont.csc380.clientserver.models.Racecar;
import edu.neumont.csc380.clientserver.models.repo.RepositoryFullException;
import edu.neumont.csc380.clientserver.models.TypedObject;
import edu.neumont.csc380.clientserver.protocol.Protocol;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Clients {
    private RemoteRepository repository;
    private List<String> keys;

    public Clients(String host, int port) {
        this.repository = new RemoteRepository(host, port);
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
        boolean serverFull = false;
        while (!serverFull) {
            String key = i + "";
            TypedObject value = random.nextBoolean() ? driver : racecar;

            try {
                this.repository.put(key, value);
                i += 1;
                this.keys.add(key);
            } catch (RepositoryFullException e) {
                serverFull = true;
            }
        }

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

    private String getRandomKey() {
        Random random = new Random();
        int randomIndex = random.nextInt(this.keys.size());
        return this.keys.get(randomIndex);
    }

    public void updateKey(String key) {
        Object value = this.repository.get(key);
        TypedObject typedValue = Protocol.deserializeTypedObject(value);
        switch (typedValue.getType()) {
            case DRIVER:
                Driver driver = (Driver) typedValue.getData();
                driver.setAge(driver.getAge() + 1);
                break;
            case RACECAR:
                Racecar racecar = (Racecar) typedValue.getData();
                racecar.setHorsePower(racecar.getHorsePower() + 1);
                break;
        }
        this.repository.update(key, value);
    }
}
