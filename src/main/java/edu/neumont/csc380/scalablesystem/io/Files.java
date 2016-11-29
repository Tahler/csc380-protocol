package edu.neumont.csc380.scalablesystem.io;

import java.io.IOException;
import java.nio.file.Paths;

public class Files {
    public static void deleteFileIfExists(String fileName) {
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
