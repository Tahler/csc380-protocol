package edu.neumont.csc380.protocol;

public interface DeserializableFromBytes<T> {
    T fromByteArray(byte[] bytes);
}
