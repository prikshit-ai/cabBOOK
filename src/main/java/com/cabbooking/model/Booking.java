package com.cabbooking.model;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int userId;
    private Cab cab; // The cab booked for this ride
    private double distance;
    private String source;
    private String destination;
    private double fare;
    private String status; // Booking status (e.g., "Released", "Booked", "Completed")
    private String paymentStatus; // Payment status (e.g., "Pending", "Paid")
    private LocalDateTime bookingTime;
    private LocalDateTime paymentTime;

    public Booking() {
        // No-argument constructor
    }

    // Constructor to initialize the booking object
    public Booking(int userId, Cab cab, double distance, String source, String destination) {
        this.userId = userId;
        this.cab = cab;
        this.distance = distance;
        this.source = source;
        this.destination = destination;
        this.bookingTime = LocalDateTime.now(); // Current time as booking time
        this.status = "Booked"; // Default status set to "Booked"
        this.paymentStatus = "Pending"; // Default payment status
        this.fare = cab.calculateFare(distance); // Calculate fare based on cab type
    }

    // Getters and Setters

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Cab getCab() {
        return cab;
    }

    public void setCab(Cab cab) {
        this.cab = cab;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    // Override toString for easy display
    @Override
    public String toString() {
        return "BookingID: " + bookingId +
                ", UserID: " + userId +
                ", Cab: " + cab.getVehicleNumber() + " (" + cab.getCabType() + ")" +
                ", Source: " + source +
                ", Destination: " + destination +
                ", Distance: " + String.format("%.2f", distance) + " km" +
                ", Fare: ₹" + String.format("%.2f", fare) +
                ", BookingTime: " + bookingTime +
                ", Status: " + status +
                ", PaymentStatus: " + paymentStatus +
                (paymentTime != null ? ", PaymentTime: " + paymentTime : "");
    }

    private int cabId;  // Add this field at the top with other fields

    public int getCabId() {
        return cabId;
    }

    public void setCabId(int cabId) {
        this.cabId = cabId;
    }

}
