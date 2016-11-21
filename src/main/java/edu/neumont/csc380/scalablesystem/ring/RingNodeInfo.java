package edu.neumont.csc380.scalablesystem.ring;

public class RingNodeInfo {
    public final String host;
    public final int port;

    public RingNodeInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public String toString() {
        return "RingNode{" +
                "port=" + port +
                '}';
    }
}
