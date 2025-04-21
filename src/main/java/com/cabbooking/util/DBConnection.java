package com.cabbooking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/CabBookingSystem";
    private static final String USER = "root";
    private static final String PASSWORD = "Prikshit@0401";

    public static Connection getConnection() throws SQLException {
        try {
            // ✅ Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Include it in your classpath!", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
