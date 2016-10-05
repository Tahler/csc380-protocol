package edu.neumont.csc380.protocol.models;

import edu.neumont.csc380.protocol.DeserializableFromBytes;
import edu.neumont.csc380.protocol.SerializableToBytes;

import java.io.*;

public class Racecar implements Serializable, SerializableToBytes, DeserializableFromBytes<Racecar> {
    private int id;
    private String make;
    private String model;
    private int horsePower;
    private double quarterMileTime;

    public Racecar(int id, String make, String model, int horsePower, double quarterMileTime) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.horsePower = horsePower;
        this.quarterMileTime = quarterMileTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getHorsePower() {
        return horsePower;
    }

    public void setHorsePower(int horsePower) {
        this.horsePower = horsePower;
    }

    public double getQuarterMileTime() {
        return quarterMileTime;
    }

    public void setQuarterMileTime(double quarterMileTime) {
        this.quarterMileTime = quarterMileTime;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return bos.toByteArray();
    }

    @Override
    public Racecar fromByteArray(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Object baseThis = null;
        try {
            ObjectInput in = new ObjectInputStream(bis);
            baseThis = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (Racecar) baseThis;
    }
}
