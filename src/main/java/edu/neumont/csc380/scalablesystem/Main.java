package edu.neumont.csc380.scalablesystem;

import edu.neumont.csc380.scalablesystem.protocol.Protocol;
import edu.neumont.csc380.scalablesystem.server.Node;

public class Main {
    public static void main(String[] args) {
        final int numUpdates = 100;

        Node node = new Node(Protocol.PORT);
        node.start();

        Clients clients = new Clients(Protocol.HOST, Protocol.PORT);
        int numKeys = clients.fillServer();
        System.out.println("Filled server with " + numKeys + " items.");

        long startTime = System.currentTimeMillis();
        clients.performUpdates(numUpdates);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        double seconds = totalTime / 1000.0;
        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");

        node.stop();
    }
}
