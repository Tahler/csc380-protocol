package edu.neumont.csc380.scalablesystem;

import edu.neumont.csc380.scalablesystem.io.Files;
import edu.neumont.csc380.scalablesystem.models.Driver;
import edu.neumont.csc380.scalablesystem.repo.RemoteRepository;
import edu.neumont.csc380.scalablesystem.ring.RingNodeInfo;
import edu.neumont.csc380.scalablesystem.ring.Spawner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Completable;

public class Main {
    public static final Logger LOGGER = LogManager.getRootLogger();

    public static void main(String[] args) throws InterruptedException {
        LOGGER.trace("Launching main...");
        Files.deleteFileIfExists(Spawner.NEXT_NODE_FILE_NAME);

        RingNodeInfo firstNode = Spawner.spawnFirstNode();

        LOGGER.trace("Spawned first node.");

        Thread.sleep(5000);

        RemoteRepository repo = new RemoteRepository(firstNode);
        // TODO: no error on put with same key
        Completable
                .merge(
                        repo.put("d1", new Driver(1, "bob", 42, true)),
                        repo.put("d2", new Driver(1, "joe", 42, true)),
                        repo.put("d3", new Driver(1, "joe", 42, true)),
                        repo.put("d4", new Driver(1, "joe", 42, true)),
                        repo.put("d5", new Driver(1, "joe", 42, true)),
                        repo.put("d6", new Driver(1, "joe", 42, true)),
                        repo.put("d7", new Driver(1, "joe", 42, true)),
                        repo.put("d8", new Driver(1, "joe", 42, true)),
                        repo.put("d9", new Driver(1, "joe", 42, true)),
                        repo.put("d10", new Driver(1, "emile", 42, true)),
                        repo.put("d11", new Driver(1, "emile", 42, true)),
                        repo.put("d12", new Driver(1, "emile", 42, true)),
                        repo.put("d13", new Driver(1, "emile", 42, true)),
                        repo.put("d14", new Driver(1, "emile", 42, true)),
                        repo.put("d15", new Driver(1, "emile", 42, true)))
                .doOnError(System.out::println)
                .subscribe();
    }
}
