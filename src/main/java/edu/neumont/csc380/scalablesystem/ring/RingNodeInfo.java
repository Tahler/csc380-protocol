package edu.neumont.csc380.scalablesystem.ring;

import java.io.Serializable;

public class RingNodeInfo implements Serializable    {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        RingNodeInfo that = (RingNodeInfo) o;

        if (this.port != that.port) {
            return false;
        }
        return this.host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = this.host.hashCode();
        result = 31 * result + this.port;
        return result;
    }

    @Override
    public String toString() {
        return "RingNode{" +
                "port=" + port +
                '}';
    }
}
