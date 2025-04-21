package com.cabbooking.model;

public class SedanCab extends Cab {

    public SedanCab(String driverName, String vehicleNumber, boolean isAvailable) {
        super(driverName, vehicleNumber, isAvailable);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * 15; // 15 units per km
    }

    @Override
    public String getCabType() {
        return "Sedan";
    }

    @Override
    public String toString() {
        return super.toString() + " CabType: Sedan";
    }
}
