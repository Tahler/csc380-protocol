package edu.neumont.csc380.protocol;

import edu.neumont.csc380.protocol.models.Driver;
import edu.neumont.csc380.protocol.models.Racecar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private Driver driver;
    private Racecar racecar;

    public Client() {
        this.driver = new Driver(123, "Mike", 40, true);
        this.racecar = new Racecar(12, "Porsche", "Cayman", 35000, 60);
    }

    public void fillServer() {
        try {
            final int numRequests = 50;
            for (int i = 0; i < numRequests; i++) {
                Request driverPut = new Request(Protocol.RequestType.PUT, i + "", this.driver);
                Protocol.ResponseType response = this.sendRequest(driverPut);
                System.out.println(response);

                Request driverGet = new Request(Protocol.RequestType.GET, i + "");
                response = this.sendRequest(driverGet);
                System.out.println(response);

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Request racecarPut = new Request(Protocol.RequestType.PUT, (i + numRequests) + "", this.racecar);
                response = this.sendRequest(racecarPut);
                System.out.println(response.name());

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Protocol.ResponseType sendRequest(Request request) {
        Protocol.ResponseType response = null;
        try (
                Socket socket = new Socket("localhost", 3000);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
        ) {
            outputStream.write(request.toByteArray());
            outputStream.write(Protocol.REQUEST_TERMINATOR); // TODO: need a different terminator, or length prefixing, or close the connection
            System.out.println("sent -1");
            int responseByte = inputStream.read();
            System.out.println("read");
            Protocol.ResponseType[] possibleResponses = Protocol.ResponseType.values();
            if (responseByte >= 0 && responseByte < possibleResponses.length) {
                response = possibleResponses[responseByte];
            } else {
                System.err.println("Impossible response value: " + responseByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.fillServer();
        System.out.println("completed");
    }
}

class Request implements SerializableToBytes {
    private Protocol.RequestType type;
    private String key;
    private SerializableToBytes value;

    /**
     * Key should not include terminator character. If it does, it must be escaped.
     */
    public Request(Protocol.RequestType type, String key) {
        this(type, key, null);
    }

    /**
     * Key should not include terminator character. If it does, it must be escaped.
     */
    public Request(Protocol.RequestType type, String key, SerializableToBytes value) {
        this.type = type;
        this.key = key + ";";
        this.value = value;
    }

    public byte[] toByteArray() {
        int typeSize = 1;
        int keySize = this.key.length();

        byte[] valueBytes = this.value == null ? new byte[0] : this.value.toByteArray();
        int dataSize = valueBytes.length;

        int totalSize = typeSize + keySize + dataSize;
        byte[] bytes = new byte[totalSize];

        bytes[0] = (byte) this.type.ordinal();
        // TODO: is getBytes in ascii?
        System.arraycopy(this.key.getBytes(), 0, bytes, 1, keySize);
        System.arraycopy(valueBytes, 0, bytes, 1 + keySize, dataSize);

        return bytes;
    }
}

class Response implements DeserializableFromBytes<Response> {
    private Protocol.ResponseType type;
    private byte[] data;

    public Response(Protocol.ResponseType type) {
        this(type, null);
    }

    public Response(Protocol.ResponseType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public Response fromByteArray(byte[] bytes) {
        int responseByte = bytes[0];
        Protocol.ResponseType type = Protocol.ResponseType.values()[responseByte];
        byte[] data = bytes.length > 1
                ? new byte[bytes.length - 1]
                : null;
        return new Response(type, data);
    }
}
