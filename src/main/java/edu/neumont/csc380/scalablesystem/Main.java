package edu.neumont.csc380.scalablesystem;

import edu.neumont.csc380.scalablesystem.ring.Node;
import edu.neumont.csc380.scalablesystem.ring.RingNodeInfo;
import edu.neumont.csc380.scalablesystem.ring.Spawner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        deleteFileIfExists(Spawner.NEXT_NODE_FILE_NAME);

        RingNodeInfo firstNode = Spawner.spawnFirstNode();

//        final int numUpdates = 100;
//
//        Clients clients = new Clients(Config.HOST, Config.START_PORT);
//        int numKeys = clients.fillServer();
//        System.out.println("Filled server with " + numKeys + " items.");
//
//        long startTime = System.currentTimeMillis();
//        clients.performUpdates(numUpdates);
//        long endTime = System.currentTimeMillis();
//        long totalTime = endTime - startTime;
//
//        double seconds = totalTime / 1000.0;
//        System.out.println("Completed " + numUpdates + " updates in " + totalTime + " millis (" + seconds + " seconds).");
    }

    private static void deleteFileIfExists(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
