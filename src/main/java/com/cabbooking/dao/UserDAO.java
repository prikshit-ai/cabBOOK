package com.cabbooking.dao;

import com.cabbooking.exceptions.InvalidLoginException;
import com.cabbooking.exceptions.UserAlreadyExistsException;
import com.cabbooking.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    // Create a new user
    boolean createUser(User user) throws UserAlreadyExistsException;

    // Get a user by their username
    User getUserByUsername(String username);

    // Update user details (e.g., password, role, etc.)
    boolean updateUserAsAdmin(int userId, String username, String password, String role) throws SQLException;
    boolean updateUserAsCustomer(int userId, String username, String password) throws SQLException;


    // Delete a user by userId
    boolean deleteUser(int userId);

    // Authenticate a user with username and password
    User authenticateUser(String username, String password) throws InvalidLoginException;

    // Get all users (for admin use)
    List<User> getAllUsers();
}

