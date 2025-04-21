package com.cabbooking.model;

public class SUVCab extends Cab {

    public SUVCab(String driverName, String vehicleNumber, boolean isAvailable) {
        super(driverName, vehicleNumber, isAvailable);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * 20; // 20 units per km
    }

    @Override
    public String getCabType() {
        return "SUV";
    }

    @Override
    public String toString() {
        return super.toString() + " CabType: SUV";
    }
}
