
package com.cabbooking.dao;

import com.cabbooking.exceptions.CabAlreadyBookedException;
import com.cabbooking.exceptions.UserAlreadyHasBookingException;
import com.cabbooking.model.Booking;
import java.util.List;

public interface BookingDAO {

    // Create a new booking
    boolean createBooking(Booking booking) throws UserAlreadyHasBookingException;

    // Get all bookings (admin use)
    List<Booking> getAllBookings();

    // Get all bookings by a specific user
    List<Booking> getBookingsByUserId(int userId);

    // Update payment status to 'Paid' and record the payment time
    boolean updatePaymentStatus(int userId);
}
