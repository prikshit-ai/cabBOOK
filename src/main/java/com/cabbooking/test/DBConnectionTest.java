package com.cabbooking.test;

import com.cabbooking.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connected to the database successfully!");
            } else {
                System.out.println("❌ Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("❌ SQL Exception: " + e.getMessage());
        }
    }
}
