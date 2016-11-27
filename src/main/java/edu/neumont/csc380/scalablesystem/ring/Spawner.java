package edu.neumont.csc380.scalablesystem.ring;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import edu.neumont.csc380.scalablesystem.Config;
import edu.neumont.csc380.scalablesystem.Serializer;
import edu.neumont.csc380.scalablesystem.repo.LocalRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Spawner {
    // A file to serve as the "spawning service"
    public static final String NEXT_NODE_FILE_NAME = "nextport";

    public static void spawn(RingNodeInfo toSpawn, RingInfo ringInfo, Map<? extends String, ?> items) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // TODO: classpath, mem, etc.
//        processBuilder.command();

        processBuilder.command("java", "Node", toSpawn.host, toSpawn.port + "");

        if (ringInfo != null) {
            String ringInfoFileName = Serializer.writeObjectToTempFile(ringInfo);
            processBuilder.command(ringInfoFileName);
        }
        if (items != null) {
            String mapFileName = Serializer.writeObjectToTempFile(ringInfo);
            processBuilder.command(mapFileName);
        }
        try {
            Process process = processBuilder.start();
            System.out.println("spawning with cmd : " + processBuilder.command());
            System.out.println("");
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
        RingNodeInfo next = Files.exists(Paths.get(NEXT_NODE_FILE_NAME))
                ? readNodeInfo()
                : new RingNodeInfo(Config.HOST, Config.START_PORT);
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
            File file = new File(NEXT_NODE_FILE_NAME);
            PrintWriter writer = new PrintWriter(file);
            writer.write(host + "\n");
            writer.write(port + "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void split(RingInfo ringInfo, LocalRepository localRepo) {
        RangeMap<Integer, RingNodeInfo> selfMap = ringInfo.getMappings();
        LocalRepository selfRepo = localRepo;

        // Send half the ranges
        int halfSize = selfRepo.size() / 2;
        TreeMap<String, Object> otherRepo = new TreeMap<>();
        // Pop "tail" off onto other until half
        while (selfRepo.size() > halfSize) {
            Map.Entry<String, Object> entry = selfRepo.pollLast();
            otherRepo.put(entry.getKey(), entry.getValue());
        }

        // Other node will be responsible for
        int selfLast = localRepo.peekLast().hashCode();
        int otherLast = otherRepo.lastKey().hashCode();
        Range<Integer> otherRange = Range.openClosed(selfLast, otherLast);

        // Update ring info
        RingNodeInfo otherRingNodeInfo = getNextNodeInfo();
        selfMap.put(otherRange, otherRingNodeInfo);
        ringInfo.timestamp();

        // Spawn the other node
        spawn(otherRingNodeInfo, ringInfo, otherRepo);
    }
}
