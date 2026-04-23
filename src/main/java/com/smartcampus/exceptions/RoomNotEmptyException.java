package com.smartcampus.exceptions;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 1
 *
 */
// Exception thrown when an attempt is made to delete a room
public class RoomNotEmptyException extends RuntimeException {

    // Constructs a new RoomNotEmptyException with a detail message.
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
