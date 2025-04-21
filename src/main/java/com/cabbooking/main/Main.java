package com.cabbooking.main;

import com.cabbooking.dao.BookingDAO;
import com.cabbooking.dao.CabDAO;
import com.cabbooking.dao.UserDAO;
import com.cabbooking.dao.impl.BookingDAOImpl;
import com.cabbooking.dao.impl.CabDAOImpl;
import com.cabbooking.dao.impl.UserDAOImpl;
import com.cabbooking.exceptions.DuplicateVehicleException;
import com.cabbooking.exceptions.InvalidLoginException;
import com.cabbooking.exceptions.UserAlreadyExistsException;
import com.cabbooking.exceptions.UserAlreadyHasBookingException;
import com.cabbooking.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAOImpl();

        int loggedInUserId = -1;
        User loggedInUser = null;

        while (true) {
            System.out.println("\n--- Cab Booking System ---");
            System.out.println("1. Login");
            System.out.println("2. Signup");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                try {
                    loggedInUser = userDAO.authenticateUser(username, password);
                    loggedInUserId = loggedInUser.getUserId();
                    System.out.println("Login successful! Welcome, " + loggedInUser.getUsername());
                    break;
                } catch (InvalidLoginException e) {
                    System.out.println("Login failed: " + e.getMessage());
                }

            } else if (choice == 2) {
                System.out.print("Choose username: ");
                String username = scanner.nextLine();
                System.out.print("Choose password: ");
                String password = scanner.nextLine();
                System.out.print("Enter role (Admin or Customer): ");
                String role = scanner.nextLine();

                User newUser;
                if ("Admin".equalsIgnoreCase(role)) {
                    newUser = new Admin(0, username, password, role);
                } else {
                    newUser = new Customer(0, username, password, role);
                }

                try {
                    boolean success = userDAO.createUser(newUser);
                    if (success) {
                        System.out.println("Signup successful! You can now log in.");
                        loggedInUser = userDAO.getUserByUsername(username);
                        loggedInUserId = loggedInUser.getUserId();
                        break;
                    } else {
                        System.out.println("Signup failed. Please try again.");
                    }
                } catch (UserAlreadyExistsException e) {
                    System.out.println("Signup failed: " + e.getMessage());
                }

            } else if (choice == 3) {
                System.out.println("Exiting... Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid choice! Try again.");
            }
        }

        // After successful login/signup
        String role = loggedInUser.getRole();

        if ("Admin".equalsIgnoreCase(role)) {
            showAdminMenu(scanner, loggedInUserId);
        } else {
            showCustomerMenu(scanner, loggedInUserId);
        }
    }

    // ADMIN MENU
    public static void showAdminMenu(Scanner scanner, int userId) {
        UserDAO userDAO = new UserDAOImpl();
        CabDAO cabDAO = new CabDAOImpl();
        BookingDAO bookingDAO = new BookingDAOImpl();
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Cab");
            System.out.println("2. Update Cab");
            System.out.println("3. Delete Cab");
            System.out.println("4. View All Cabs");
            System.out.println("5. Get Cab By Vehicle Number");
            System.out.println("6. View All Bookings");
            System.out.println("7. View All Users");
            System.out.println("8. Update User Details");
            System.out.println("9. Delete User");
            System.out.println("10. Logout");

            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addCab(scanner, cabDAO);
                    break;
                case 2:
                    updateCab(scanner, cabDAO);
                    break;
                case 3:
                    deleteCab(scanner, cabDAO);
                    break;
                case 4:
                    viewAllCabs(cabDAO);
                    break;
                case 5:
                    getCabByVehicleNumber(cabDAO, scanner);
                    break;
                case 6:
                    viewAllBookings(cabDAO, bookingDAO);
                    break;
                case 7:
                    viewAllUsers(userDAO);
                    break;
                case 8:
                    updateUserDetailsAsAdmin(scanner, userDAO);
                    break;
                case 9:
                    deleteUser(scanner, userDAO);
                    break;
                case 10:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }


    // CUSTOMER MENU
    public static void showCustomerMenu(Scanner scanner, int userId) {
        UserDAO userDAO = new UserDAOImpl();
        CabDAO cabDAO = new CabDAOImpl();
        BookingDAO bookingDAO = new BookingDAOImpl();

        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. Book a Cab");
            System.out.println("2. View My Bookings");
            System.out.println("3. Make Payment");
            System.out.println("4. View Available Cabs");
            System.out.println("5. Update My Details");
            System.out.println("6. Delete My Account");
            System.out.println("7. Logout");

            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    bookCab(scanner, bookingDAO, userId); // Call the function to book a cab
                    break;
                case 2:
                    viewMyBookings(bookingDAO, userId); // Logic to view bookings
                    break;
                case 3:
                    makePayment(scanner, bookingDAO, userId); // Logic to make a payment
                    break;
                case 4:
                    viewAvailableCabs(cabDAO); // Logic to view available cabs
                    break;
                case 5:
                    updateUserDetails(userDAO, scanner, userId); // Logic to update user details
                    break;
                case 6:
                    deleteOwnAccount(userDAO, scanner, userId); // Logic to delete the user account
                    return; // Exit the menu after account deletion
                case 7:
                    System.out.println("Logging out...");
                    return; // Exit the menu when logging out
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
    public static void addCab(Scanner scanner, CabDAO cabDAO) {
        try {
            System.out.print("Enter driver name: ");
            String driverName = scanner.nextLine();

            System.out.print("Enter vehicle number: ");
            String vehicleNumber = scanner.nextLine();

            System.out.print("Enter cab type (MiniCab, Sedan, SUV): ");
            String cabType = scanner.nextLine().trim();

            // Set the availability to true by default
            boolean isAvailable = true;

            // Create the Cab object based on the cab type
            Cab cab = null;
            switch (cabType.toLowerCase()) {
                case "minicab":
                    cab = new MiniCab(driverName, vehicleNumber, isAvailable);
                    break;
                case "sedan":
                    cab = new SedanCab(driverName, vehicleNumber, isAvailable);
                    break;
                case "suv":
                    cab = new SUVCab(driverName, vehicleNumber, isAvailable);
                    break;
                default:
                    System.out.println("Invalid cab type. Please choose from MiniCab, Sedan, or SUV.");
                    return;
            }

            // Attempt to add the cab using the DAO method and handle success message in Main
            boolean success = cabDAO.addCab(cab);
            if (success) {
                System.out.println("Cab added successfully!");
            } else {
                System.out.println("Failed to add cab.");
            }
        } catch (DuplicateVehicleException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error adding cab: " + e.getMessage());
        }
    }
    public static void updateCab(Scanner scanner, CabDAO cabDAO) {
        try {
            System.out.print("Enter the Cab ID to update: ");
            int cabId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter new driver name: ");
            String driverName = scanner.nextLine();

            System.out.print("Enter new vehicle number: ");
            String vehicleNumber = scanner.nextLine();

            System.out.print("Enter new cab type (Mini, Sedan, SUV): ");
            String cabType = scanner.nextLine().trim();

            System.out.print("Is the cab available? (true/false): ");
            boolean isAvailable = Boolean.parseBoolean(scanner.nextLine());

            cabDAO.updateCab(cabId, driverName, vehicleNumber, isAvailable, cabType);

        } catch (NumberFormatException e) {
            System.out.println("Invalid cab ID format. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error occurred while updating the cab.");
        }
    }
    public static void deleteCab(Scanner scanner, CabDAO cabDAO) {
        System.out.print("Enter the vehicle number of the cab to delete: ");
        String vehicleNumber = scanner.nextLine().trim();

        System.out.print("Are you sure you want to delete cab with vehicle number " + vehicleNumber + "? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            try {
                cabDAO.deleteCab(vehicleNumber);
            } catch (Exception e) {
                System.out.println("Error deleting cab: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    public static void viewAllCabs(CabDAO cabDAO) {
        List<Cab> allCabs = cabDAO.getAllCabs();
        if (allCabs.isEmpty()) {
            System.out.println("No cabs found.");
        } else {
            System.out.println("\n--- All Cabs ---");
            for (Cab cab : allCabs) {
                System.out.println(cab);  // relies on Cab.toString() for one‑line output
            }
        }
    }
    public static void getCabByVehicleNumber(CabDAO cabDAO, Scanner scanner) {
        System.out.print("Enter the vehicle number: ");
        String vehicleNumber = scanner.nextLine();

        Cab cab = cabDAO.getCabByVehicleNumber(vehicleNumber);

        if (cab == null) {
            System.out.println("Cab with vehicle number " + vehicleNumber + " not found.");
        } else {
            System.out.println("Cab details:");
            System.out.println(cab);  // This will print the details using the Cab's toString method
        }
    }
    public static void viewAllBookings(CabDAO cabDAO, BookingDAO bookingDAO) {
        // Get all bookings from the database
        List<Booking> bookings = bookingDAO.getAllBookings();

        // Check if there are any bookings
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
        } else {
            // Iterate over the bookings and display each one
            for (Booking booking : bookings) {
                System.out.println("--------------------------------------------------------");
                System.out.println("Booking ID: " + booking.getBookingId());
                System.out.println("User ID: " + booking.getUserId());
                System.out.println("Cab ID: " + booking.getCab().getCabId());
                System.out.println("Driver: " + booking.getCab().getDriverName());
                System.out.println("Vehicle Number: " + booking.getCab().getVehicleNumber());
                System.out.println("Cab Type: " + booking.getCab().getCabType());
                System.out.println("Distance: " + booking.getDistance() + " km");
                System.out.println("Source: " + booking.getSource());
                System.out.println("Destination: " + booking.getDestination());
                System.out.println("Fare: ₹" + booking.getFare());
                System.out.println("Status: " + booking.getStatus());
                System.out.println("Payment Status: " + booking.getPaymentStatus());
                System.out.println("Booking Time: " + booking.getBookingTime());
                System.out.println("Payment Time: " + (booking.getPaymentTime() != null ? booking.getPaymentTime() : "N/A"));
            }
            System.out.println("--------------------------------------------------------");
        }
    }
    public static void viewAllUsers(UserDAO userDAO) {
        List<User> users = userDAO.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("\n--- All Users ---");
            for (User u : users) {
                // user.toString() already gives: userId: X, username: Y, role: Z
                System.out.println(u);
            }
        }
    }
    public static void updateUserDetailsAsAdmin(Scanner sc, UserDAO userDAO) {
        try {
            System.out.print("Enter User ID to update: ");
            int userId = Integer.parseInt(sc.nextLine());

            System.out.print("New username: ");
            String newUsername = sc.nextLine();

            System.out.print("New password: ");
            String newPassword = sc.nextLine();

            System.out.print("New role (Admin/Customer): ");
            String newRole = sc.nextLine();

            boolean ok = userDAO.updateUserAsAdmin(userId, newUsername, newPassword, newRole);
            if (ok) {
                System.out.println("User updated.");
            } else {
                System.out.println("No such user, or nothing changed.");
            }
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }
    public static void deleteUser(Scanner scanner, UserDAO userDAO) {
        System.out.print("Enter the User ID to delete: ");
        int userId;
        try {
            userId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be a number.");
            return;
        }

        System.out.print("Are you sure you want to delete user #" + userId + "? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = userDAO.deleteUser(userId);
        if (deleted) {
            System.out.println("User " + userId + " deleted successfully.");
        } else {
            System.out.println("No user found with ID " + userId + ".");
        }
    }
    public static void bookCab(Scanner scanner, BookingDAO bookingDAO, int userId) {
        // we need a Booking object with just the userId set
        Booking booking = new Booking();
        booking.setUserId(userId);

        try {
            boolean success = bookingDAO.createBooking(booking);
            if (success) {
                System.out.println("Booking completed!");
            } else {
                System.out.println("Booking failed.");
            }
        } catch (UserAlreadyHasBookingException e) {
            System.err.println("Cannot book: " + e.getMessage());
        }
    }
    public static void viewMyBookings(BookingDAO bookingDAO, int userId) {
        // Fetch all bookings for the user
        List<Booking> bookings = bookingDAO.getBookingsByUserId(userId);

        if (bookings.isEmpty()) {
            System.out.println("You have no bookings.");
        } else {
            System.out.println("\n--- Your Bookings ---");
            for (Booking booking : bookings) {
                // Display the booking details
                System.out.println("Booking ID: " + booking.getBookingId());
                System.out.println("Cab ID: " + booking.getCab().getCabId());
                System.out.println("Driver: " + booking.getCab().getDriverName());
                System.out.println("Vehicle Number: " + booking.getCab().getVehicleNumber());
                System.out.println("Cab Type: " + booking.getCab().getCabType());
                System.out.println("Source: " + booking.getSource());
                System.out.println("Destination: " + booking.getDestination());
                System.out.println("Distance: " + booking.getDistance() + " km");
                System.out.println("Fare: ₹" + booking.getFare());
                System.out.println("Status: " + booking.getStatus());
                System.out.println("Payment Status: " + booking.getPaymentStatus());
                System.out.println("Booking Time: " + booking.getBookingTime());
                if (booking.getPaymentTime() != null) {
                    System.out.println("Payment Time: " + booking.getPaymentTime());
                }
                System.out.println("-------------------------");
            }
        }
    }
    public static void makePayment(Scanner scanner, BookingDAO bookingDAO, int userId) {
        System.out.println("\n----- Make Payment for Booking -----");
        boolean success = bookingDAO.updatePaymentStatus(userId);

        if (success) {
            System.out.println("Payment process completed successfully.");
        } else {
            System.out.println("Payment process failed or no pending bookings found.");
        }
    }
    public static void viewAvailableCabs(CabDAO cabDAO) {
        System.out.println("\n----- Available Cabs -----");

        List<Cab> availableCabs = cabDAO.getAvailableCabs();

        if (availableCabs.isEmpty()) {
            System.out.println("No cabs are currently available.");
            return;
        }

        for (Cab cab : availableCabs) {
            System.out.println("Cab ID: " + cab.getCabId() +
                    " | Driver: " + cab.getDriverName() +
                    " | Vehicle No: " + cab.getVehicleNumber() +
                    " | Type: " + cab.getCabType());
        }
    }
    public static void updateUserDetails(UserDAO userDAO, Scanner scanner, int userId) {
        System.out.println("\n----- Update User Details -----");

        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        try {
            boolean isUpdated = userDAO.updateUserAsCustomer(userId, newUsername, newPassword);

            if (isUpdated) {
                System.out.println("User details updated successfully.");
            } else {
                System.out.println("Failed to update user details. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("Error while updating user: " + e.getMessage());
        }
    }
    public static void deleteOwnAccount(UserDAO userDAO, Scanner scanner, int userId) {
        System.out.println("\nAre you sure you want to delete your account? This action cannot be undone.");
        System.out.print("Type 'DELETE' to confirm: ");
        String confirmation = scanner.nextLine();

        if (!"DELETE".equalsIgnoreCase(confirmation)) {
            System.out.println("Account deletion cancelled.");
            return;
        }

        boolean deleted = userDAO.deleteUser(userId);
        if (deleted) {
            System.out.println("Your account has been successfully deleted. Goodbye!");
            System.exit(0); // Exit program after deletion
        } else {
            System.out.println("Failed to delete your account. Please try again later.");
        }
    }




}
