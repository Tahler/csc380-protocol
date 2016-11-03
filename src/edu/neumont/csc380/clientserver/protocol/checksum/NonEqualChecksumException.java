package edu.neumont.csc380.clientserver.protocol.checksum;

public class NonEqualChecksumException extends RuntimeException {
    public NonEqualChecksumException() {
        super();
    }

    public NonEqualChecksumException(String message) {
        super(message);
    }
}
