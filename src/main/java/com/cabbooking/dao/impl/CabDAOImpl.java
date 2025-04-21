package com.cabbooking.dao.impl;

import com.cabbooking.dao.CabDAO;
import com.cabbooking.exceptions.DuplicateVehicleException;
import com.cabbooking.model.*;
import com.cabbooking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CabDAOImpl implements CabDAO {

    @Override
    public boolean addCab(Cab cab) throws DuplicateVehicleException {
        String checkSql = "SELECT COUNT(*) FROM Cabs WHERE vehicle_number = ?";
        String insertSql = "INSERT INTO Cabs (driver_name, vehicle_number, is_available, cab_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             // 1) Check for duplicate
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, cab.getVehicleNumber());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DuplicateVehicleException(
                            "A cab with vehicle number " + cab.getVehicleNumber() + " already exists.");
                }
            }

            // 2) No duplicate: insert new cab
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, cab.getDriverName());
                insertStmt.setString(2, cab.getVehicleNumber());
                insertStmt.setBoolean(3, cab.isAvailable()); // This will be true as per default
                insertStmt.setString(4, cab.getCabType());

                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void updateCab(int cabId, String driverName, String vehicleNumber, boolean isAvailable, String cabType) {
        String sql = "UPDATE Cabs SET driver_name = ?, vehicle_number = ?, is_available = ?, cab_type = ? WHERE cab_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driverName);
            stmt.setString(2, vehicleNumber);
            stmt.setBoolean(3, isAvailable);
            stmt.setString(4, cabType);
            stmt.setInt(5, cabId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cab details updated successfully.");
            } else {
                System.out.println("Cab with ID " + cabId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error occurred while updating the cab.");
        }
    }

    @Override
    public void deleteCab(String vehicleNumber) {
        String sql = "DELETE FROM Cabs WHERE vehicle_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleNumber);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Cab with Vehicle Number " + vehicleNumber + " deleted successfully.");
            } else {
                System.out.println("Cab with Vehicle Number " + vehicleNumber + " not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Cab> getAllCabs() {
        List<Cab> allCabs = new ArrayList<>(); // List to hold all cabs
        String sql = "SELECT * FROM Cabs"; // SQL query to get all cabs

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Extracting the data for each cab
                int cabId = rs.getInt("cab_id");
                String driverName = rs.getString("driver_name");
                String vehicleNumber = rs.getString("vehicle_number");
                boolean isAvailable = rs.getBoolean("is_available");
                String cabType = rs.getString("cab_type");

                // Create a new Cab object and add it to the list
                Cab cab = null;
                if ("Mini".equals(cabType)) {
                    cab = new MiniCab(driverName, vehicleNumber, isAvailable);
                } else if ("Sedan".equals(cabType)) {
                    cab = new SedanCab(driverName, vehicleNumber, isAvailable);
                } else if ("SUV".equals(cabType)) {
                    cab = new SUVCab(driverName, vehicleNumber, isAvailable);
                }

                if (cab != null) {
                    cab.setCabId(cabId);
                    allCabs.add(cab);
                }
            }

        } catch (SQLException e) {
            // Handle exceptions by printing the error message
            System.err.println("Error fetching all cabs: " + e.getMessage());
        }

        return allCabs; // Return the list of cabs
    }



    @Override
    public List<Cab> getAvailableCabs() {
        List<Cab> availableCabs = new ArrayList<>(); // List to hold available cabs
        String sql = "SELECT * FROM Cabs WHERE is_available = TRUE"; // SQL query to get available cabs

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Extracting the data for each cab
                int cabId = rs.getInt("cab_id");
                String driverName = rs.getString("driver_name");
                String vehicleNumber = rs.getString("vehicle_number");
                boolean isAvailable = rs.getBoolean("is_available");
                String cabType = rs.getString("cab_type");

                // Create a new Cab object and add it to the list
                Cab cab = null;
                if ("Mini".equals(cabType)) {
                    cab = new MiniCab(driverName, vehicleNumber, isAvailable);
                } else if ("Sedan".equals(cabType)) {
                    cab = new SedanCab(driverName, vehicleNumber, isAvailable);
                } else if ("SUV".equals(cabType)) {
                    cab = new SUVCab(driverName, vehicleNumber, isAvailable);
                }

                if (cab != null) {
                    cab.setCabId(cabId); // Setting the cabId for the created cab
                    availableCabs.add(cab); // Add to the list of available cabs
                }
            }

        } catch (SQLException e) {
            // Handle exceptions by printing the error message
            System.err.println("Error fetching available cabs: " + e.getMessage());
        }

        return availableCabs; // Return the list of available cabs
    }


    @Override
    public Cab getCabByVehicleNumber(String vehicleNumber) {
        Cab cab = null; // Initialize the cab to null
        String sql = "SELECT * FROM Cabs WHERE vehicle_number = ?"; // SQL query to get a cab by vehicle number

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the vehicle number in the prepared statement
            stmt.setString(1, vehicleNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extracting the data for the cab
                    int cabId = rs.getInt("cab_id");
                    String driverName = rs.getString("driver_name");
                    boolean isAvailable = rs.getBoolean("is_available");
                    String cabType = rs.getString("cab_type");

                    // Create a new Cab object based on the cab type
                    if ("Mini".equals(cabType)) {
                        cab = new MiniCab(driverName, vehicleNumber, isAvailable);
                    } else if ("Sedan".equals(cabType)) {
                        cab = new SedanCab(driverName, vehicleNumber, isAvailable);
                    } else if ("SUV".equals(cabType)) {
                        cab = new SUVCab(driverName, vehicleNumber, isAvailable);
                    }

                    if (cab != null) {
                        cab.setCabId(cabId); // Set cabId for the found cab
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching cab by vehicle number: " + e.getMessage());
        }

        return cab; // Return the found cab or null if not found
    }


    @Override
    public boolean updateCabAvailability(String vehicleNumber, boolean isAvailable) {
        String sql = "UPDATE Cabs SET is_available = ? WHERE vehicle_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isAvailable);
            stmt.setString(2, vehicleNumber);

            int rowsUpdated = stmt.executeUpdate();  // Get the number of affected rows

            if (rowsUpdated > 0) {
                System.out.println("Cab availability updated successfully!");
                return true;  // Return true if update is successful
            } else {
                System.out.println("No cab found with the specified vehicle number.");
                return false;  // Return false if no rows were updated
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return false;  // Return false if there was a SQL exception
        }
    }

}
