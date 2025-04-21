package com.cabbooking.exceptions;

public class UserAlreadyHasBookingException extends Exception {
    public UserAlreadyHasBookingException(String message) {
        super(message);
    }
}

