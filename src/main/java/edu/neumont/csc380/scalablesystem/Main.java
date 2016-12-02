package edu.neumont.csc380.scalablesystem;

import edu.neumont.csc380.scalablesystem.io.Files;
import edu.neumont.csc380.scalablesystem.logging.Env;
import edu.neumont.csc380.scalablesystem.models.Driver;
import edu.neumont.csc380.scalablesystem.ring.RemoteRepository;
import edu.neumont.csc380.scalablesystem.ring.RingInfo;
import edu.neumont.csc380.scalablesystem.ring.Spawner;
import rx.Completable;

public class Main {
    public static void main(String[] args) {
        Env.LOGGER.trace("Launching main...");
        Files.deleteFileIfExists(Spawner.NEXT_VNODE_FILE_NAME);

        Spawner.spawnNextVnode(new RingInfo())
                .subscribe(firstNode -> {
                    Env.LOGGER.debug("Spawned first node.");

                    RemoteRepository repo = new RemoteRepository(firstNode);
                    // TODO: no error here on put with same key
                    Completable
                            .merge(
                                    repo.put("d7", new Driver(1, "joe", 42, true)),
                                    repo.put("d2", new Driver(1, "joe", 42, true)),
                                    repo.put("d13", new Driver(1, "emile", 42, true)),
                                    repo.put("d19", new Driver(1, "emile", 42, true)),
                                    repo.put("d4", new Driver(1, "joe", 42, true)),
                                    repo.put("d5", new Driver(1, "joe", 42, true)),
                                    repo.put("d6", new Driver(1, "joe", 42, true)),
                                    repo.put("d8", new Driver(1, "joe", 42, true)),
                                    repo.put("d3", new Driver(1, "joe", 42, true)),
                                    repo.put("d10", new Driver(1, "emile", 42, true)),
                                    repo.put("d11", new Driver(1, "emile", 42, true)),
                                    repo.put("d12", new Driver(1, "emile", 42, true)),
                                    repo.put("d9", new Driver(1, "joe", 42, true)),
                                    repo.put("d14", new Driver(1, "emile", 42, true)),
                                    repo.put("d16", new Driver(1, "emile", 42, true)),
                                    repo.put("d17", new Driver(1, "emile", 42, true)),
                                    repo.put("d18", new Driver(1, "emile", 42, true)),
                                    repo.put("d34", new Driver(1, "emile", 42, true)),
                                    repo.put("d27", new Driver(1, "emile", 42, true)),
                                    repo.put("d21", new Driver(1, "emile", 42, true)),
                                    repo.put("d1", new Driver(1, "bob", 42, true)),
                                    repo.put("d37", new Driver(1, "emile", 42, true)),
                                    repo.put("d24", new Driver(1, "emile", 42, true)),
                                    repo.put("d22", new Driver(1, "emile", 42, true)),
                                    repo.put("d23", new Driver(1, "emile", 42, true)),
                                    repo.put("d15", new Driver(1, "snowflake15", 42, true)),
                                    repo.put("d26", new Driver(1, "emile", 42, true)),
                                    repo.put("d36", new Driver(1, "emile", 42, true)),
                                    repo.put("d28", new Driver(1, "emile", 42, true)),
                                    repo.put("d29", new Driver(1, "snowflake", 42, false)),
                                    repo.put("d38", new Driver(1, "emile", 42, true)),
                                    repo.put("d31", new Driver(1, "emile", 42, true)),
                                    repo.put("d32", new Driver(1, "emile", 42, true)),
                                    repo.put("d33", new Driver(1, "emile", 42, true)),
                                    repo.put("d35", new Driver(1, "emile", 42, true)),
                                    repo.put("d25", new Driver(1, "emile", 42, true)),
                                    repo.put("d30", new Driver(1, "emile", 42, true)),
                                    repo.put("d13", new Driver(1, "emile", 42, true)),
                                    repo.put("d39", new Driver(1, "emile", 42, true)),
                                    repo.put("d40", new Driver(1, "emile", 42, true)))
                            .doOnError(System.out::println)
                            .await();

                    System.out.println("ALL PUT");

//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    repo.get("d15").subscribe(System.out::println);
                });
    }
}
