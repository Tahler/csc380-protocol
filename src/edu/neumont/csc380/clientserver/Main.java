package edu.neumont.csc380.clientserver;

import edu.neumont.csc380.clientserver.models.Driver;
import edu.neumont.csc380.clientserver.models.TypedObject;

public class Main {
    public static void main(String[] args) {
//        main_withoutLocking();
//        main_proofOfConcurrentUpdate();
        main_withLocking();
    }

    private static void main_withoutLocking() {
        final int numUpdates = 100;

        Server server = new Server(false);
        server.start();

        Clients clients = new Clients();
        int numKeys = clients.fillServer();
        System.out.println("Filled server with " + numKeys + " items.");

        long startTime = System.currentTimeMillis();
        clients.performUpdates(numUpdates);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");

        server.stop();
    }

    private static void main_proofOfConcurrentUpdate() {
        final int numUpdates = 100;

        Server server = new Server(false);
        server.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client.putObjectOnServer("0", new TypedObject<>(TypedObject.Type.DRIVER, new Driver(123, "Mike", 0, true)));

        long startTime = System.currentTimeMillis();
        Clients clients = new Clients();
        clients.performUpdatesOnKey(numUpdates, "0");
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        TypedObject object = Client.getObjectOnServer("0");
        Driver driver = (Driver) object.getData();
        System.out.println(driver.getAge());

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");

        server.stop();
    }

    private static void main_withLocking() {
        final int numUpdates = 100;

        Server server = new Server(true);
        server.start();

        Clients clients = new Clients();
        int numKeys = clients.fillServer();
        System.out.println("Filled server with " + numKeys + " items.");

        long startTime = System.currentTimeMillis();
        clients.performUpdates(numUpdates);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");

        server.stop();
    }
}
