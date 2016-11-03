package edu.neumont.csc380.clientserver.protocol.checksum;

import edu.neumont.csc380.clientserver.protocol.serialization.ByteArrayConverter;

import java.io.Serializable;

public class ChecksumCalculator {
    public static final int CHECKSUM_LENGTH = 4;

    public static int calculate32BitChecksum(byte[] bytes) {
        int sum = 0;
        for (byte bite : bytes) {
            sum += bite;
        }
        return sum;
    }

    public static int calculate32BitChecksum(Serializable object) {
        byte[] bytes = ByteArrayConverter.toByteArray(object);
        return calculate32BitChecksum(bytes);
    }

    public static boolean checksumIsCorrect(byte[] bytes, int checksum) {
        int recalculatedChecksum = calculate32BitChecksum(bytes);
        return checksum == recalculatedChecksum;
    }
}
