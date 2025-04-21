package com.cabbooking.model;

public abstract class Cab {
    private int cabId; // Added cabId
    private String driverName;
    private String vehicleNumber;
    private boolean isAvailable;

    // Constructors
    public Cab() {
        // Default constructor
    }

    public Cab(int cabId, String driverName, String vehicleNumber, boolean isAvailable) { // Updated constructor
        this.cabId = cabId;
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.isAvailable = isAvailable;
    }

    public Cab(String driverName, String vehicleNumber, boolean isAvailable) {
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.isAvailable = isAvailable;
    }

    // Abstract method to calculate fare
    public abstract double calculateFare(double distance);
    public abstract String getCabType();

    // Getters and Setters

    public int getCabId() { // Getter for cabId
        return cabId;
    }

    public void setCabId(int cabId) { // Setter for cabId
        this.cabId = cabId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "Cab ID: " + cabId + ", Driver: " + driverName + ", Vehicle Number: " + vehicleNumber + ", Available: " + isAvailable;
    }
}