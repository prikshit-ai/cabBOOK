package com.cabbooking.dao.impl;

import com.cabbooking.dao.UserDAO;
import com.cabbooking.exceptions.InvalidLoginException;
import com.cabbooking.exceptions.UserAlreadyExistsException;
import com.cabbooking.model.User;
import com.cabbooking.model.Admin;
import com.cabbooking.model.Customer;
import com.cabbooking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // Create a new user
    @Override
    public boolean createUser(User user) throws UserAlreadyExistsException {
        String checkSql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        String insertSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, user.getUsername());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new UserAlreadyExistsException("Username '" + user.getUsername() + "' already exists.");
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, user.getUsername());
                insertStmt.setString(2, user.getPassword());
                insertStmt.setString(3, user.getRole());

                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    // Get a user by their username
    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String password = rs.getString("password");
                    String role = rs.getString("role");

                    // Create a user object based on role (Admin or Customer)
                    if ("Admin".equalsIgnoreCase(role)) {
                        return new Admin(userId, username, password, role);
                    } else {
                        return new Customer(userId, username, password, role);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
        }

        return null;
    }

    // Update user details (e.g., password, role, etc.)
    @Override
    public boolean updateUserAsAdmin(int userId, String username, String password, String role) throws SQLException {
        String query = "UPDATE Users SET username = ?, password = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setInt(4, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public boolean updateUserAsCustomer(int userId, String username, String password) throws SQLException {
        String query = "UPDATE Users SET username = ?, password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }


    // Delete a user by userId
    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }

        return false;
    }

    // Authenticate a user with username and password
    @Override
    public User authenticateUser(String username, String password) throws InvalidLoginException {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    // no matching row → login fails
                    throw new InvalidLoginException("Invalid username or password.");
                }

                int userId = rs.getInt("user_id");
                String role  = rs.getString("role");
                if ("Admin".equalsIgnoreCase(role)) {
                    return new Admin(userId, username, password, role);
                } else {
                    return new Customer(userId, username, password, role);
                }
            }

        } catch (SQLException e) {
            // wrap SQL errors as runtime
            throw new RuntimeException("Database error during authentication", e);
        }
    }

    // Get all users (for admin use)
    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");

                // Create user object based on role
                if ("Admin".equalsIgnoreCase(role)) {
                    users.add(new Admin(userId, username, password, role));
                } else {
                    users.add(new Customer(userId, username, password, role));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }

        return users;
    }
}
