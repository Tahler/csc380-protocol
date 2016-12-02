package edu.neumont.csc380.scalablesystem;

import edu.neumont.csc380.scalablesystem.io.Files;
import edu.neumont.csc380.scalablesystem.logging.Env;

import java.io.*;

public class Serializer {
    public static String writeObjectToTempFile(Object object) {
        File tempFile = null;
        ObjectOutputStream oos = null;
        try {
            Env.LOGGER.debug("creating temp file");
            tempFile = File.createTempFile("tmp", ".obj");
            String tempFileName = tempFile.getName();
            Env.LOGGER.debug("Writing temp obj: " + object + " to " + tempFileName);

            oos = new ObjectOutputStream(new FileOutputStream(tempFileName));
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
            Env.LOGGER.debug("aw shit it was " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile == null
                ? null
                : tempFile.getName();
    }

    public static <T> T consumeObjectFromTempFile(String fileName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            Object readObject = ois.readObject();
            Env.LOGGER.debug("deleting " + fileName + "...");
            Files.deleteFileIfExists(fileName);
            Env.LOGGER.debug("...deleted " + fileName + ".");
            T decoded = (T) readObject;
            Env.LOGGER.debug("cast to " + decoded);
            return decoded;
        } catch (Exception e) {
            Env.LOGGER.fatal(e);
            throw new RuntimeException(e);
        }
    }
}
