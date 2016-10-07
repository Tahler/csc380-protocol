package edu.neumont.csc380.clientserver.protocol.io;

import edu.neumont.csc380.clientserver.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;

public class StringReader {
    private InputStream inputStream;

    public StringReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Reads a <code>String</code> from the input stream that is terminated by <code>Protocol.STRING_TERMINATOR</code>.
     * If the input stream ends early (i.e. the connection is closed), the method will return what it has so far.
     *
     * The String that is returned will not include the terminator, and all escaped characters will be translated.
     */
    public String readString() {
        StringBuilder builder = new StringBuilder();
        boolean expectingMoreString = true;
        boolean escaping = false;
        do {
            int ascii;
            try {
                ascii = this.inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (ascii >= 0) {
                char translation = (char) ascii;
                if (escaping) {
                    if (translation == Protocol.ESCAPE_CHARACTER) {
                        builder.append(translation);
                    }
                    escaping = false;
                } else if (translation == Protocol.ESCAPE_CHARACTER) {
                    escaping = true;
                } else if (translation == Protocol.STRING_TERMINATOR) {
                    expectingMoreString = false;
                } else {
                    builder.append(translation);
                }
            } else {
                expectingMoreString = false;
            }
        }
        while (expectingMoreString);
        return builder.toString();
    }
}
