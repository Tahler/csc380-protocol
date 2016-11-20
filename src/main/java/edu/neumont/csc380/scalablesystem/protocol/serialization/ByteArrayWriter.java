package edu.neumont.csc380.scalablesystem.protocol.serialization;

import com.hallaLib.HallaZip;
import edu.neumont.csc380.scalablesystem.protocol.checksum.ChecksumCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Compresses and checksums a byte array across an output stream.
 */
public class ByteArrayWriter {
    private Socket socket;

    public ByteArrayWriter(Socket socket) {
        this.socket = socket;
    }

    public void write(byte[] bytes) {
        try {
            OutputStream outputStream = this.socket.getOutputStream();
            InputStream inputStream = this.socket.getInputStream();

            boolean successfullyRead;
            do {
                // Checksum must be on the NON-COMPRESSED bytes
                int checksum = ChecksumCalculator.calculate32BitChecksum(bytes);
                byte[] compressedBytes = HallaZip.compress(bytes);
                outputStream.write(ByteArrayConverter.toByteArray(compressedBytes.length));
                outputStream.write(compressedBytes);
                outputStream.write(ByteArrayConverter.toByteArray(checksum));

                // keep connection open, read a response.
                // if 0, close, if 1, send again.
                int responseByte = inputStream.read();
                successfullyRead = responseByte == 0;
            } while (!successfullyRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
