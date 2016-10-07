package edu.neumont.csc380.clientserver.protocol.io;

import edu.neumont.csc380.clientserver.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StringWriter {
    private OutputStream outputStream;

    public StringWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Writes the string in a protocol-compatible manner.
     *
     * The string that is sent will be terminated and escaped.
     */
    public void writeString(String string) {
        String escapeChar = String.valueOf(Protocol.ESCAPE_CHARACTER);
        String escapedEscapeChar = escapeChar + escapeChar;

        String terminatorChar = String.valueOf(Protocol.STRING_TERMINATOR);
        String escapedTerminatorChar = escapeChar + terminatorChar;

        String allEscaped = string
                .replace(escapeChar, escapedEscapeChar)
                .replace(terminatorChar, escapedTerminatorChar);
        String allTerminated = allEscaped + ";";

        byte[] asciiBytes = allTerminated.getBytes(StandardCharsets.US_ASCII);
        try {
            this.outputStream.write(asciiBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
