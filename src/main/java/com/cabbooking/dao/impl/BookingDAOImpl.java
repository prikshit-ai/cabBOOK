package com.cabbooking.dao.impl;

import com.cabbooking.dao.BookingDAO;
import com.cabbooking.exceptions.CabAlreadyBookedException;
import com.cabbooking.exceptions.UserAlreadyHasBookingException;
import com.cabbooking.model.Booking;
import com.cabbooking.model.Cab;
import com.cabbooking.model.MiniCab;  // Concrete subclass
import com.cabbooking.model.SedanCab;  // Concrete subclass
import com.cabbooking.model.SUVCab;    // Concrete subclass
import com.cabbooking.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookingDAOImpl implements BookingDAO {

    // Helper method to create a Cab instance based on cabType
    private Cab createCab(String cabType, String driverName, String vehicleNumber, boolean isAvailable) {
        switch (cabType) {
            case "Mini":
                return new MiniCab(driverName, vehicleNumber, isAvailable); // MiniCab constructor
            case "Sedan":
                return new SedanCab(driverName, vehicleNumber, isAvailable); // SedanCab constructor
            case "SUV":
                return new SUVCab(driverName, vehicleNumber, isAvailable); // SUVCab constructor
            default:
                throw new IllegalArgumentException("Unknown cab type: " + cabType);
        }
    }

    @Override
    public boolean createBooking(Booking booking) throws UserAlreadyHasBookingException {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Ask user for source, destination, and distance
        System.out.print("Enter source: ");
        String source = scanner.nextLine();

        System.out.print("Enter destination: ");
        String destination = scanner.nextLine();

        System.out.print("Enter distance (in km): ");
        double distance = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        // Step 2: Display available cabs and calculate fare using Cab object
        String fareQuery = "SELECT * FROM Cabs WHERE is_available = true";
        List<Cab> availableCabs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(fareQuery);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nAvailable cabs:");
            while (rs.next()) {
                String driverName = rs.getString("driver_name");
                String vehicleNumber = rs.getString("vehicle_number");
                String cabType = rs.getString("cab_type");
                boolean isAvailable = rs.getBoolean("is_available");

                Cab cab = createCab(cabType, driverName, vehicleNumber, isAvailable);
                cab.setCabId(rs.getInt("cab_id"));
                availableCabs.add(cab);

                double fare = cab.calculateFare(distance);
                System.out.println("Vehicle: " + vehicleNumber + " | Type: " + cabType + " | Fare: ₹" + fare);
            }

            if (availableCabs.isEmpty()) {
                System.out.println("No cabs available at the moment.");
                return false;
            }

            // Step 3: Ask user to choose a cab
            System.out.print("\nEnter vehicle number to book: ");
            String selectedVehicleNumber = scanner.nextLine();
            Cab selectedCab = null;

            for (Cab cab : availableCabs) {
                if (cab.getVehicleNumber().equalsIgnoreCase(selectedVehicleNumber)) {
                    selectedCab = cab;
                    break;
                }
            }

            if (selectedCab == null) {
                System.out.println("Invalid vehicle number selected.");
                return false;
            }


            // Step 4: Set booking time and prepare insert
            booking.setBookingTime(LocalDateTime.now());
            double fare = selectedCab.calculateFare(distance);
            String checkUserBookingSQL = "SELECT * FROM Bookings WHERE user_id = ? AND status = 'Booked'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserBookingSQL)) {
                checkStmt.setInt(1, booking.getUserId());
                try (ResultSet resultSet = checkStmt.executeQuery()) {
                    if (resultSet.next()) {
                        throw new UserAlreadyHasBookingException("User already has an active booking.");
                    }
                }
            }

            String sql = "INSERT INTO Bookings (user_id, cab_id, distance, source, destination, fare, status, payment_status, booking_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt2 = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt2.setInt(1, booking.getUserId());
                stmt2.setInt(2, selectedCab.getCabId());
                stmt2.setDouble(3, distance);
                stmt2.setString(4, source);
                stmt2.setString(5, destination);
                stmt2.setDouble(6, fare);
                stmt2.setString(7, "Booked");
                stmt2.setString(8, "Pending");
                stmt2.setTimestamp(9, Timestamp.valueOf(booking.getBookingTime()));

                int rowsAffected = stmt2.executeUpdate();

                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt2.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            booking.setBookingId(generatedKeys.getInt(1));

                            // Step 5: Mark cab as unavailable
                            String updateCabSql = "UPDATE Cabs SET is_available = false WHERE cab_id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateCabSql)) {
                                updateStmt.setInt(1, selectedCab.getCabId());
                                updateStmt.executeUpdate();
                            }

                            System.out.println("Booking successful. Booking ID: " + booking.getBookingId());
                            return true;
                        }
                    }
                }

            }

        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }

        return false;
    }


    // Update payment status and payment time for the booking
    @Override
    public boolean updatePaymentStatus(int userId) {
        Scanner scanner = new Scanner(System.in);
        List<Booking> pendingBookings = new ArrayList<>();
        String query = "SELECT * FROM Bookings WHERE user_id = ? AND status = 'Booked' AND payment_status = 'Pending'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nYour Pending Bookings:");
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setCabId(rs.getInt("cab_id"));
                booking.setDistance(rs.getDouble("distance"));
                booking.setSource(rs.getString("source"));
                booking.setDestination(rs.getString("destination"));
                booking.setFare(rs.getDouble("fare"));
                booking.setStatus(rs.getString("status"));
                booking.setPaymentStatus(rs.getString("payment_status"));
                booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());

                Timestamp paymentTimeStamp = rs.getTimestamp("payment_time");
                if (paymentTimeStamp != null) {
                    booking.setPaymentTime(paymentTimeStamp.toLocalDateTime());
                }

                pendingBookings.add(booking);

                System.out.println("Booking ID: " + booking.getBookingId() +
                        " | From: " + booking.getSource() +
                        " | To: " + booking.getDestination() +
                        " | Fare: ₹" + booking.getFare());
            }

            if (pendingBookings.isEmpty()) {
                System.out.println("No pending bookings found.");
                return false;
            }

            System.out.print("\nEnter Booking ID to mark as Paid: ");
            int bookingId = scanner.nextInt();

            // Now update that booking's payment status and mark the cab as available
            String updateQuery = "UPDATE Bookings SET payment_status = ?, payment_time = ?, status = 'Released' WHERE booking_id = ?";
            String updateCabQuery = "UPDATE Cabs SET is_available = true WHERE cab_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement updateCabStmt = conn.prepareStatement(updateCabQuery)) {

                // Update the payment status
                updateStmt.setString(1, "Paid");
                updateStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                updateStmt.setInt(3, bookingId);

                // Execute payment status update
                int rows = updateStmt.executeUpdate();

                if (rows > 0) {
                    // Get cab ID from the booking to update cab availability
                    int cabId = pendingBookings.stream()
                            .filter(b -> b.getBookingId() == bookingId)
                            .findFirst()
                            .map(Booking::getCabId)
                            .orElseThrow(() -> new SQLException("Cab not found for booking"));

                    // Update cab availability
                    updateCabStmt.setInt(1, cabId);
                    updateCabStmt.executeUpdate();

                    return true;
                } else {
                    System.out.println("Booking ID not found or payment already completed.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error updating payment: " + e.getMessage());
        }

        return false;
    }




    // Get all bookings (admin use)
    @Override
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.user_id, b.cab_id, b.distance, b.source, b.destination, b.fare, " +
                "b.status, b.payment_status, b.booking_time, b.payment_time, c.driver_name, c.vehicle_number, c.cab_type, c.is_available " +
                "FROM Bookings b INNER JOIN Cabs c ON b.cab_id = c.cab_id"; // Fetch bookings with cab details

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Extract data from the result set
                int bookingId = rs.getInt("booking_id");
                int userId = rs.getInt("user_id");
                int cabId = rs.getInt("cab_id");
                double distance = rs.getDouble("distance");
                String source = rs.getString("source");
                String destination = rs.getString("destination");
                double fare = rs.getDouble("fare");
                String status = rs.getString("status");
                String paymentStatus = rs.getString("payment_status");
                Timestamp bookingTimeTimestamp = rs.getTimestamp("booking_time");
                Timestamp paymentTimeTimestamp = rs.getTimestamp("payment_time");

                // Get the cab details
                String driverName = rs.getString("driver_name");
                String vehicleNumber = rs.getString("vehicle_number");
                String cabType = rs.getString("cab_type");
                boolean isAvailable = rs.getBoolean("is_available");

                // Create a Cab object based on cabType
                Cab cab = createCab(cabType, driverName, vehicleNumber, isAvailable);
                cab.setCabId(cabId);

                // Create a Booking object
                Booking booking = new Booking(userId, cab, distance, source, destination);
                booking.setBookingId(bookingId);
                booking.setFare(fare);
                booking.setStatus(status);
                booking.setPaymentStatus(paymentStatus);
                booking.setBookingTime(bookingTimeTimestamp.toLocalDateTime());
                booking.setPaymentTime(paymentTimeTimestamp != null ? paymentTimeTimestamp.toLocalDateTime() : null);

                // Add the booking to the list
                bookings.add(booking);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
        }

        return bookings;
    }

    // Get all bookings by a specific user
    @Override
    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.cab_id, b.distance, b.source, b.destination, b.fare, " +
                "b.status, b.payment_status, b.booking_time, b.payment_time, c.driver_name, c.vehicle_number, c.cab_type, c.is_available " +
                "FROM Bookings b INNER JOIN Cabs c ON b.cab_id = c.cab_id WHERE b.user_id = ?"; // Fetch bookings for a specific user

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId); // Set userId parameter

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Extract data from the result set
                    int bookingId = rs.getInt("booking_id");
                    int cabId = rs.getInt("cab_id");
                    double distance = rs.getDouble("distance");
                    String source = rs.getString("source");
                    String destination = rs.getString("destination");
                    double fare = rs.getDouble("fare");
                    String status = rs.getString("status");
                    String paymentStatus = rs.getString("payment_status");
                    Timestamp bookingTimeTimestamp = rs.getTimestamp("booking_time");
                    Timestamp paymentTimeTimestamp = rs.getTimestamp("payment_time");

                    // Get the cab details
                    String driverName = rs.getString("driver_name");
                    String vehicleNumber = rs.getString("vehicle_number");
                    String cabType = rs.getString("cab_type");
                    boolean isAvailable = rs.getBoolean("is_available");

                    // Create a Cab object based on cabType
                    Cab cab = createCab(cabType, driverName, vehicleNumber, isAvailable);
                    cab.setCabId(cabId);

                    // Create a Booking object
                    Booking booking = new Booking(userId, cab, distance, source, destination);
                    booking.setBookingId(bookingId);
                    booking.setFare(fare);
                    booking.setStatus(status);
                    booking.setPaymentStatus(paymentStatus);
                    booking.setBookingTime(bookingTimeTimestamp.toLocalDateTime());
                    booking.setPaymentTime(paymentTimeTimestamp != null ? paymentTimeTimestamp.toLocalDateTime() : null);

                    // Add the booking to the list
                    bookings.add(booking);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bookings for user " + userId + ": " + e.getMessage());
        }

        return bookings;
    }
}
