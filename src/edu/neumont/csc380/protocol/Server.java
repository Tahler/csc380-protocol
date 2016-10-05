package edu.neumont.csc380.protocol;

import com.hallaLib.HallaStor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private HallaStor repository;

    public Server() {
        this.repository = HallaStor.getInstance();
    }

    public void start() {
        try {
            ServerSocket server = new ServerSocket(3000);
            while (true) {
                Socket client = server.accept();
                new Thread(() -> handle(client)).start();
            }
        } catch (IOException e) {
            System.out.println("Failed to start server on port 3000.");
            e.printStackTrace();
        }
    }

    public void handle(Socket client) {
        System.out.println("Accepted connection");
        try {
            InputStream inputStream = client.getInputStream();

            int bite = inputStream.read();
            Protocol.RequestType[] operations = Protocol.RequestType.values();
            boolean isValidOperation = bite >= 0 && bite < operations.length;

            String key = null;
            if (isValidOperation) {
                Protocol.RequestType selectedOperation = operations[bite];

                StringBuilder stringBuilder = new StringBuilder();
                boolean escaping = false;
                while (key == null && (bite = inputStream.read()) != -1) {
                    char character = (char) bite;

                    if (character == Protocol.ESCAPE_CHARACTER) {
                        if (escaping) {
                            stringBuilder.append(character);
                        } else {
                            escaping = true;
                        }
                    }
                    if (!escaping && character == Protocol.STRING_TERMINATOR) {
                        key = stringBuilder.toString();
                    } else {
                        stringBuilder.append(character);
                    }
                }
                if (key == null) {
                    this.respond(client, Protocol.ResponseType.INVALID_KEY);
                } else {
                    if (selectedOperation == Protocol.RequestType.GET) {
                        if (this.repository.containsKey(key)) {
                            // TODO: double check casting
                            byte[] blob = (byte[]) this.repository.get(key);
                            this.respond(client, Protocol.ResponseType.SUCCESS, blob);
                        } else {
                            this.respond(client, Protocol.ResponseType.KEY_DOES_NOT_EXIST);
                        }
                    } else if (selectedOperation == Protocol.RequestType.PUT) {
                        if (this.repository.containsKey(key)) {
                            System.out.println("contains");
                            this.respond(client, Protocol.ResponseType.KEY_ALREADY_EXISTS);
                        } else {
                            byte[] data = remainingBytesFromInputStream(inputStream);
                            try {
                                System.out.println("success");
                                this.repository.add(key, data);
                                this.respond(client, Protocol.ResponseType.SUCCESS);
                                System.out.println("sent success");
                            } catch (Exception e) {
                                // TODO: remove once you know what exception it is
                                e.printStackTrace();
                                this.respond(client, Protocol.ResponseType.SERVER_FULL);
                            }
                        }
                    }
                }
            } else {
                this.respond(client, Protocol.ResponseType.INVALID_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] remainingBytesFromInputStream(InputStream inputStream) throws IOException {
        List<Byte> byteList = new ArrayList<>();
        int bite;
        System.out.println("classic me");
        while ((bite = inputStream.read()) != -1) {
            byteList.add((byte) bite);
        }
        System.out.println("jk");
        Byte[] wrapperBytes = byteList.toArray(new Byte[0]);
        byte[] bytes = new byte[wrapperBytes.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = wrapperBytes[i];
        }
        return bytes;
    }

    public void respond(Socket client, Protocol.ResponseType responseType) {
        this.respond(client, responseType, null);
    }

    public void respond(Socket client, Protocol.ResponseType responseType, byte[] data) {
        try {
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(responseType.ordinal());
            if (data != null) {
                outputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
