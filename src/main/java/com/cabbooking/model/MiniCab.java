package com.cabbooking.model;

public class MiniCab extends Cab {

    public MiniCab(String driverName, String vehicleNumber, boolean isAvailable) {
        super(driverName, vehicleNumber, isAvailable);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * 10; // 10 units per km
    }

    @Override
    public String getCabType() {
        return "Mini";
    }

    @Override
    public String toString() {
        return super.toString() + " CabType: Mini";
    }
}