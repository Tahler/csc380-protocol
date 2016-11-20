package edu.neumont.csc380.scalablesystem.protocol.serialization;

import com.hallaLib.HallaZip;
import edu.neumont.csc380.scalablesystem.protocol.checksum.ChecksumCalculator;
import edu.neumont.csc380.scalablesystem.protocol.checksum.NonEqualChecksumException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ByteArrayReader {
    private Socket socket;

    public ByteArrayReader(Socket socket) {
        this.socket = socket;
    }

    /**
     * Assumes it can at least read a length
     * @throws NonEqualChecksumException Thrown if the calculated checksum is incorrect.
     */
    public byte[] read() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = this.socket.getOutputStream();

            boolean successfullyRead;
            byte[] decompressedBytes;
            do {
                int length = this.readNextInt();

                byte[] compressedBytes = new byte[length];
                inputStream.read(compressedBytes, 0, length);

                decompressedBytes = HallaZip.expand(compressedBytes);
                int expectedChecksum = this.readNextInt();
                successfullyRead = ChecksumCalculator.checksumIsCorrect(decompressedBytes, expectedChecksum);

                int responseByte = successfullyRead ? 0 : 1;
                outputStream.write(responseByte);
            } while (!successfullyRead);

            return decompressedBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int readNextInt() {
        try {
            byte[] bytes = new byte[4];
            this.socket.getInputStream().read(bytes, 0, 4);
            return ByteArrayConverter.readInt(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
