package edu.neumont.csc380.clientserver.protocol.serialization;

import com.hallaLib.HallaZip;
import edu.neumont.csc380.clientserver.protocol.checksum.ChecksumCalculator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Compresses and checksums a byte array across an output stream.
 */
public class ByteArrayWriter {
    private OutputStream outputStream;

    public ByteArrayWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(byte[] bytes) {
        // Checksum must be on the NON-COMPRESSED bytes
        int checksum = ChecksumCalculator.calculate32BitChecksum(bytes);
        byte[] compressedBytes = HallaZip.compress(bytes);
        try {
            this.outputStream.write(ByteArrayConverter.toByteArray(compressedBytes.length));
            this.outputStream.write(compressedBytes);
            this.outputStream.write(ByteArrayConverter.toByteArray(checksum));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
