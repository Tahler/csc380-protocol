package edu.neumont.csc380.clientserver.models;

public class Racecar {
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
}
