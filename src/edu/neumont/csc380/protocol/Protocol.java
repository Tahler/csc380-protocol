package edu.neumont.csc380.protocol;

public class Protocol {
    public static final int REQUEST_TERMINATOR = -1;
    public static final char ESCAPE_CHARACTER = '\\';
    public static final char STRING_TERMINATOR = ';';

    public enum RequestType {
        GET,
        PUT
    }

    public enum ResponseType {
        SUCCESS,
        INVALID_REQUEST,
        INVALID_KEY,
        KEY_DOES_NOT_EXIST,
        KEY_ALREADY_EXISTS,
        SERVER_FULL
    }
}
