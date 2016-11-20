package edu.neumont.csc380.scalablesystem.protocol.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class ByteArrayConverter {
    public static byte[] toByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    public static byte[] toByteArray(Serializable object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (
                ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)
        ) {
            oos.writeObject(object);
            oos.flush();

            byte[] bytes = byteArrayOutputStream.toByteArray();
            // TODO: should be in finally block?
            byteArrayOutputStream.close();
            oos.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int readInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }
}
