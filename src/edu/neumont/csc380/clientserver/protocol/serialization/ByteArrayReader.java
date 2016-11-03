package edu.neumont.csc380.clientserver.protocol.serialization;

import com.hallaLib.HallaZip;
import edu.neumont.csc380.clientserver.protocol.checksum.ChecksumCalculator;
import edu.neumont.csc380.clientserver.protocol.checksum.NonEqualChecksumException;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayReader {
    private InputStream inputStream;

    public ByteArrayReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Assumes it can at least read a length
     * @throws NonEqualChecksumException Thrown if the calculated checksum is incorrect.
     */
    public byte[] read() {
        try {
            int length = this.readNextInt();

            byte[] compressedBytes = new byte[length];
            this.inputStream.read(compressedBytes, 0, length);

            byte[] decompressedBytes = HallaZip.expand(compressedBytes);
            int expectedChecksum = this.readNextInt();
            boolean isCorrect = ChecksumCalculator.checksumIsCorrect(decompressedBytes, expectedChecksum);
            if (!isCorrect) {
                throw new NonEqualChecksumException();
            }

            return decompressedBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int readNextInt() {
        try {
            byte[] bytes = new byte[4];
            this.inputStream.read(bytes, 0, 4);
            return ByteArrayConverter.readInt(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
