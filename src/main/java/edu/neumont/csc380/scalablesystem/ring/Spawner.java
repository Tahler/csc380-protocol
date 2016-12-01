package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.Range;
import edu.neumont.csc380.scalablesystem.Config;
import edu.neumont.csc380.scalablesystem.Serializer;
import edu.neumont.csc380.scalablesystem.comparator.HashComparator;
import edu.neumont.csc380.scalablesystem.repo.LocalRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Spawner {
    // A file to serve as the "spawning service"
    public static final String NEXT_NODE_FILE_NAME = "next-node.tmp";

    // TODO: should be completable to know when node has started
    public static void spawn(RingNodeInfo toSpawn, RingInfo ringInfo, Map<? extends String, ?> items) {
        if (Node.LOGGER != null) {
            Node.LOGGER.debug("SPAWNING: " + toSpawn + "\n with " + ringInfo + "\n and " + items);
        }
        // TODO: memory, etc.
        List<String> argsList = new ArrayList<>(4);
        argsList.add(toSpawn.host);
        argsList.add(toSpawn.port + "");

        if (ringInfo != null) {
            assert items != null;

            Node.LOGGER.debug("writing RingInfo");
            String ringInfoFileName = Serializer.writeObjectToTempFile(ringInfo);
            argsList.add(ringInfoFileName);

            Node.LOGGER.debug("writing items");
            String mapFileName = Serializer.writeObjectToTempFile(items);
            argsList.add(mapFileName);
        }
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
            if (Node.LOGGER != null) {
                Node.LOGGER.debug("SPAWNING - " + processBuilder.command());
            }
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RingNodeInfo spawnFirstNode() {
        RingNodeInfo toSpawn = getNextNodeInfo();
        spawn(toSpawn, null, null);
        return toSpawn;
    }

    private static RingNodeInfo getNextNodeInfo() {
        if (Node.LOGGER != null) {
            Node.LOGGER.debug("GETTING NODE INFO");
        }
        RingNodeInfo next = Files.exists(Paths.get(NEXT_NODE_FILE_NAME))
                ? readNodeInfo()
                : new RingNodeInfo(Config.HOST, Config.START_PORT);
        if (Node.LOGGER != null) {
            Node.LOGGER.debug("GOT: " + next);
        }
        writeNodeInfo(next.host, next.port + 1);
        return next;
    }

    private static RingNodeInfo readNodeInfo() {
        try {
            File file = new File(NEXT_NODE_FILE_NAME);
            Scanner reader = new Scanner(new FileReader(file));
            String host = reader.nextLine();
            int port = reader.nextInt();
            return new RingNodeInfo(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeNodeInfo(String host, int port) {
        try {
            if (Node.LOGGER != null) {
                Node.LOGGER.debug("WRITING NODE INFO: " + host + ":" + port);
            }

            File file = new File(NEXT_NODE_FILE_NAME);
            PrintWriter writer = new PrintWriter(file);
            writer.write(host + "\n");
            writer.write(port + "");
            writer.flush();
            writer.close();
            if (Node.LOGGER != null) {
                Node.LOGGER.debug("done.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void split(RingInfo ringInfo, LocalRepository localRepo) {
//        RangeMap<Integer, RingNodeInfo> selfMap = ringInfo.getMappings();
        LocalRepository selfRepo = localRepo;

        // Send half the ranges
        int halfSize = selfRepo.size() / 2;
        Node.LOGGER.debug("SPLIT - half size = " + halfSize);
        TreeMap<String, Object> otherMap = new TreeMap<>(new HashComparator<>());

        // Pop "tail" off onto other until half
        while (selfRepo.size() > halfSize) {
            Map.Entry<String, Object> entry = selfRepo.pollLast();
            otherMap.put(entry.getKey(), entry.getValue());
        }
        Node.LOGGER.debug("SPLIT - other repo: " + otherMap);

        // Other node will be responsible for
        int selfLast = localRepo.peekLast().getKey().hashCode();
        int otherLast = otherMap.lastKey().hashCode();

        Node.LOGGER.debug("SPLIT - creating range: " + selfLast + " .. " + otherLast);

        // Assign Infinity to the other range if this node's range went to infinity
        Range<Integer> otherRange = hashBelongsToLastNode(otherLast, ringInfo)
                ? Range.greaterThan(selfLast)
                : Range.openClosed(selfLast, otherLast);

        Node.LOGGER.debug("SPLIT - other range: " + otherRange);

        // Update ring info
        RingNodeInfo otherRingNodeInfo = getNextNodeInfo();
        Node.LOGGER.debug("SPLIT - adding node to ring: " + otherRingNodeInfo);
        ringInfo.addNode(otherRange, otherRingNodeInfo);

        Node.LOGGER.debug("SPLIT - updated ring: " + ringInfo);

        // Spawn the other node
        spawn(otherRingNodeInfo, ringInfo, otherMap);
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
