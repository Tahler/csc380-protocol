package edu.neumont.csc380.scalablesystem.protocol.checksum;

public class NonEqualChecksumException extends RuntimeException {
    public NonEqualChecksumException() {
        super();
    }

    public NonEqualChecksumException(String message) {
        super(message);
    }
}
