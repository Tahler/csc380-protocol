package edu.neumont.csc380.scalablesystem;

import java.io.*;

public class Serializer {
    public static String writeObjectToTempFile(Object object) {
        File tempFile = null;
        ObjectOutputStream oos = null;
        try {
            tempFile = File.createTempFile("", ".obj");
            String tempFileName = tempFile.getName();

            oos = new ObjectOutputStream(new FileOutputStream(tempFileName));
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
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
            return (T) readObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
