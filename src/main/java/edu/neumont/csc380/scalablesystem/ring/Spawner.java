package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.Range;
import edu.neumont.csc380.scalablesystem.Config;
import edu.neumont.csc380.scalablesystem.Serializer;
import edu.neumont.csc380.scalablesystem.comparator.HashComparator;
import edu.neumont.csc380.scalablesystem.logging.Env;
import rx.Completable;
import rx.Single;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Spawner {
    // A file to serve as the "spawning service"
    public static final String NEXT_VNODE_FILE_NAME = "next-vnode.tmp";

    public static Completable spawnNode(RingNodeInfo toSpawn, RingInfo ringInfo) {
        Env.LOGGER.debug("SPAWNING: " + toSpawn + "\n with " + ringInfo);
        // TODO: memory, etc.
        List<String> argsList = new ArrayList<>(3);
        argsList.add(toSpawn.host);
        argsList.add(toSpawn.port + "");

        String ringInfoFileName = Serializer.writeObjectToTempFile(ringInfo);
        argsList.add(ringInfoFileName);

        String args = String.join(" ", argsList);
        String command = String.format(
                "mvn exec:java " +
                        "-Dexec.mainClass=\"edu.neumont.csc380.scalablesystem.ring.Node\" " +
                        "-Dexec.args=\"%s\"", args);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(Arrays.asList(
                    "bash",
                    "-c",
                    command));
            Env.LOGGER.debug("SPAWNING - " + processBuilder.command());
            Process process = processBuilder.start();

            return Completable.create(subscriber -> {
                InputStream stdout = process.getInputStream();
                BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
                Completable spawnCompletable = Completable.create(stdoutSubscriber -> {
                    try {
                        // TODO: shouldn't be doing this without also waiting for stderr / exit
                        while (!"STARTED".equals(stdoutBuffered.readLine())) ;
                        stdoutSubscriber.onCompleted();
                    } catch (IOException e) {
                        stdoutSubscriber.onError(new RuntimeException("Something went wrong spawning " + toSpawn.host + ":" + toSpawn.port));
                    }
                });
                spawnCompletable.await();
                subscriber.onCompleted();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Completable spawnVnode(RingNodeInfo firstNode, RingInfo ringInfo) {
        RingNodeInfo toSpawn = firstNode;
        List<RingNodeInfo> nodesToSpawn = new ArrayList<>(Config.VNODE_SIZE);
        for (int i = 0; i < Config.VNODE_SIZE; i++) {
            nodesToSpawn.add(toSpawn);
            toSpawn = RingInfo.getNextNodeInRing(toSpawn);
        }
        writeNextVnodeInfo(toSpawn.host, toSpawn.port);

        List<Completable> spawnCompletables = nodesToSpawn.stream()
                .map(node -> spawnNode(node, ringInfo))
                .collect(Collectors.toList());
        return Completable.merge(spawnCompletables);
    }

    public static Single<RingNodeInfo> spawnNextVnode(RingInfo ringInfo) {
        RingNodeInfo firstNode = getNextVnodeInfo();
        return spawnVnode(firstNode, ringInfo)
                .andThen(Single.just(firstNode));
    }

    private static RingNodeInfo getNextVnodeInfo() {
        Env.LOGGER.debug("GETTING NODE INFO");
        RingNodeInfo next = Files.exists(Paths.get(NEXT_VNODE_FILE_NAME))
                ? readNextVnodeInfo()
                : new RingNodeInfo(Config.HOST, Config.START_PORT);
        Env.LOGGER.debug("GOT: " + next);
        writeNextVnodeInfo(next.host, next.port + 2);
        return next;
    }

    private static RingNodeInfo readNextVnodeInfo() {
        try {
            File file = new File(NEXT_VNODE_FILE_NAME);
            Scanner reader = new Scanner(new FileReader(file));
            String host = reader.nextLine();
            int port = reader.nextInt();
            return new RingNodeInfo(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeNextVnodeInfo(String host, int port) {
        try {
            if (Env.LOGGER != null) {
                Env.LOGGER.debug("WRITING NODE INFO: " + host + ":" + port);
            }

            File file = new File(NEXT_VNODE_FILE_NAME);
            PrintWriter writer = new PrintWriter(file);
            writer.write(host + "\n");
            writer.write(port + "");
            writer.flush();
            writer.close();
            if (Env.LOGGER != null) {
                Env.LOGGER.debug("done.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Completable splitVnode(RingInfo ringInfo, VnodeRepository repo) {
        // TODO: this could all be wrapped in a completable thread?

        // Send half the ranges
        int halfSize = repo.size() / 2;
        Env.LOGGER.debug("SPLIT - half size = " + halfSize);
        TreeMap<String, Object> otherMap = new TreeMap<>(new HashComparator<>());

        // Pop "tail" off onto other until half
        while (repo.size() > halfSize) {
            repo.pollLast()
                    .subscribe(entry -> otherMap.put(entry.getKey(), entry.getValue()));
        }
        Env.LOGGER.debug("SPLIT - other repo: " + otherMap);

        return Completable.create(subscriber -> {
            repo.peekLast()
                    .subscribe(entry -> {
                        int repoLast = entry.getKey().hashCode();
                        int otherLast = otherMap.lastKey().hashCode();

                        Env.LOGGER.debug("SPLIT - creating range: " + repoLast + " .. " + otherLast);

                        // Assign Infinity to the other range if this node's range went to infinity
                        Range<Integer> otherRange = hashBelongsToLastNode(otherLast, ringInfo)
                                ? Range.greaterThan(repoLast)
                                : Range.openClosed(repoLast, otherLast);

                        Env.LOGGER.debug("SPLIT - other range: " + otherRange);

                        // Update ring info
                        RingNodeInfo otherRingNodeInfo = getNextVnodeInfo();
                        Env.LOGGER.debug("SPLIT - adding node to ring: " + otherRingNodeInfo);
                        ringInfo.addNode(otherRange, otherRingNodeInfo);

                        Env.LOGGER.debug("SPLIT - updated ring: " + ringInfo);

                        // Spawn the other node
                        spawnVnode(otherRingNodeInfo, ringInfo).await();

                        // Add all kvps
                        RemoteIntercomRepository intercom = new RemoteIntercomRepository(otherRingNodeInfo);
                        List<Completable> puts = otherMap.entrySet().stream()
                                .map(otherEntry -> intercom.put(otherEntry.getKey(), otherEntry.getValue()))
                                .collect(Collectors.toList());

                        Completable.merge(puts).subscribe(subscriber::onCompleted);
                    });
        });
    }

    // TODO: move methods to more appropriate location
    private static boolean hashBelongsToLastNode(Integer hash, RingInfo ringInfo) {
        RingNodeInfo nodeWithKey = ringInfo.getMappings().get(hash);
        return isLastNode(nodeWithKey, ringInfo);
    }

    private static boolean isLastNode(RingNodeInfo node, RingInfo ringInfo) {
        RingNodeInfo lastNode = ringInfo.getMappings().get(Integer.MAX_VALUE);
        return node.equals(lastNode);
    }
}
