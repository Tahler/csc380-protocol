package edu.neumont.csc380.protocol.models;

import edu.neumont.csc380.protocol.DeserializableFromBytes;
import edu.neumont.csc380.protocol.SerializableToBytes;

import java.io.*;

public class Driver implements Serializable, SerializableToBytes, DeserializableFromBytes<Driver> {
    private int id;
    private String name;
    private int age;
    private boolean isMale;

    public Driver(int id, String name, int age, boolean isMale) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.isMale = isMale;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
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
    public Driver fromByteArray(byte[] bytes) {
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
        return (Driver) baseThis;
    }
}
